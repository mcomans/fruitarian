package nl.tudelft.fruitarian.observers

import java.util.concurrent.Executors

import nl.tudelft.fruitarian.models.{DCnet, NetworkInfo}
import nl.tudelft.fruitarian.p2p.{Address, TCPHandler}
import nl.tudelft.fruitarian.p2p.messages.{FruitarianMessage, NextRoundMessage, ResultMessage, TextMessage, TransmitMessage, TransmitRequest}
import nl.tudelft.fruitarian.patterns.Observer

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, ExecutionContextExecutorService, Future, Promise}
import scala.util.{Random, Try}

/**
 * This class handles the Transmission message phase. This means that for each
 * 'round' it should reply with a random message in case it doesn't want to
 * send a message and it should reply with a it's message in encrypted form if
 * a message has been queued.
 */
class TransmissionObserver(handler: TCPHandler, networkInfo: NetworkInfo) extends Observer[FruitarianMessage] {
  protected implicit val context: ExecutionContextExecutorService =
    ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(16))
  var messageRound: Promise[Boolean] = _
  val MESSAGE_ROUND_TIMEOUT = 5000
  val BACKOFF_RANGE = 10
  var messageQueue = new mutable.Queue[String]()
  var messageSent: String = ""
  var backoff = 0
  var roundId = 0

  /**
   * Add a message to the message queue.
   * The message will be sent in the next round if the backoff is not set.
   * @param message The message to be sent.
   * @param prioritise Whether to prioritise this message (aka send it as
   *                   soon as possible instead of adding it in the back of
   *                   the queue).
   */
  def queueMessage(message: String, prioritise: Boolean = false): Unit = {
    if (message.length > DCnet.MESSAGE_SIZE) {
      throw new Exception("Message too long.")
    }
    if (prioritise) {
      messageQueue = mutable.Queue[String](message) ++ messageQueue
    } else {
      messageQueue.enqueue(message)
    }
  }

  /**
   * Starts a message round if the current node is the center node.
   *
   * The message round asks each node in the clique (including itself) to send
   * message using the TransmitRequest message. The amount of requests sent is
   * noted in the DCNet, however this is a soft verification as the protocol
   * could break if one or more of the nodes have an outdated cliquePeers list.
   * Resulting in nonsense messages.
   *
   * Upon receiving a TransmitRequest all nodes will return a TransmitMessage
   * with their message or a random value based on the random seeds between
   * the peers. If exactly one of the nodes sent a message instead of a random
   * value this message can be decrypted on the centre node without knowing
   * which node sent it (see DCNet code).
   */
  def startMessageRound(): Unit = {
    // Clear possible remaining responses.
    DCnet.clearResponses()
    messageRound = Promise[Boolean]()

    // Send a TransmitRequest to all peers and itself (as this node is also part of the clique).
    sendMessageToClique((address: Address) => TransmitRequest(networkInfo.ownAddress, address, roundId))

    // Set the amount of requests sent.
    DCnet.transmitRequestsSent = networkInfo.cliquePeers.length + 1

    println(s"[S] [${networkInfo.nodeId}] Started Message round for ${DCnet.transmitRequestsSent} node(s).")

    Future {
      // TODO: Find a better way to sleep, this seems to cause the logs to be somewhat delayed.
      Thread.sleep(MESSAGE_ROUND_TIMEOUT)
      if (!messageRound.isCompleted) {
        messageRound failure (_)
        roundId += 1
        println("[S] Message round timed out, retrying...")

        // Send a "TIMEOUT" Text message to all peers to let them know the
        // round failed and trigger the message requeue behaviour if one of
        // them actually sent a message this round.
        sendMessageToClique((address: Address) => ResultMessage(networkInfo.ownAddress, address, "TIMEOUT"))

        // Give some additional time, and retry.
        Thread.sleep(MESSAGE_ROUND_TIMEOUT)
        startNextRound(roundId)
      }
    }

  }

  /**
   * Helper function to send a message to all clique peers and itself.
   */
  def sendMessageToClique(msg: (Address) => FruitarianMessage): Unit = {
    networkInfo.cliquePeers.foreach(p => handler.sendMessage(msg(p.address)))
    handler.sendMessage(msg(networkInfo.ownAddress))
  }

  def startNextRound(roundId: Int): Unit = {
    val nextCenter = networkInfo.getNextPeer
    nextCenter match {
      case Some(p) => handler.sendMessage(NextRoundMessage(networkInfo.ownAddress, p.address, roundId))
      case None => startMessageRound()
    }
  }


  override def receiveUpdate(event: FruitarianMessage): Unit = event match {
    case TransmitRequest(from, to, reqRoundId) =>
      // Set the roundId to the highest value, as our random generators can only generate extra values.
      roundId = math.max(roundId, reqRoundId)
      if (messageQueue.nonEmpty && messageSent.isEmpty && backoff == 0 && roundId == reqRoundId) {
        // If we have a message to send and are not waiting for confirmation
        // of a previous message, send the next message. If we failed to send
        // a message and have a backoff we have to wait this cycle.
        // TODO: This 'just-send-it' behaviour can cause collisions, as
        //  multiple nodes could send a message at the same time. It wil also
        //  produce nonsense messages in case no one sends an actual encrypted
        //  message.
        messageSent = messageQueue.dequeue()
        println(s"[C][R$reqRoundId] Sent my message: '$messageSent'")
        handler.sendMessage(TransmitMessage(to, from, (reqRoundId, DCnet
          .encryptMessage(messageSent, networkInfo.cliquePeers.toList,
            reqRoundId))))
      } else {
        // Else send a random message.
        handler.sendMessage(TransmitMessage(to, from, (reqRoundId, DCnet.getRandom
        (networkInfo.cliquePeers.toList, reqRoundId))))
      }
      // Decrease the backoff by one until 0.
      backoff = math.max(0, backoff - 1)

    case TransmitMessage(_, _, message) =>
      this.synchronized {
        if (message._1 == roundId) {
          // Only add the message if it matches the round id.
          DCnet.appendResponse(message._2)
        }
      }
      if (this.synchronized { DCnet.canDecrypt }) {
        val decryptedMessage = DCnet.decryptReceivedMessages()
          // Send the decrypted message to the clique.
        Await.ready(sendMessageToClique((address: Address) => ResultMessage
        (networkInfo.ownAddress, address, decryptedMessage)), 100.millis)

        // This sleep is required before starting the next round, likely for
        // different threads to start. It gives clients the time to handle
        // the result message.
        Thread.sleep(5)
        startNextRound(roundId)
      }

    case ResultMessage(_, _, msg) if !messageSent.isEmpty =>
      // If we recently sent a message, the next TextMessage received should be
      // this message. If not we need to resend the message.
      if (msg != messageSent) {
        // If the message is not as sent, queue the message for sending again.
        // We apply a random backoff in amount of cycles to hopefully prevent
        // another collision.
        queueMessage(messageSent, true)
        backoff = new Random().nextInt(BACKOFF_RANGE)
        println(s"[C] Message not sent correctly, enqueued again in $backoff cycles.")
      }
      // Unblock the message sending process to allow the next message or a resend.
      messageSent = ""

    case NextRoundMessage(_, _, r) =>
      roundId = r
      startMessageRound()
    case _ =>
  }
}
