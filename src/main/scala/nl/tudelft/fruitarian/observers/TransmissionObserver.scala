package nl.tudelft.fruitarian.observers

import nl.tudelft.fruitarian.models.{DCnet, NetworkInfo}
import nl.tudelft.fruitarian.p2p.{Address, TCPHandler}
import nl.tudelft.fruitarian.p2p.messages.{FruitarianMessage, TextMessage, TransmitMessage, TransmitRequest}
import nl.tudelft.fruitarian.patterns.Observer

import scala.collection.mutable
import scala.concurrent.{Future, Promise}
import scala.util.{Random, Try}

/**
 * This class handles the Transmission message phase. This means that for each
 * 'round' it should reply with a random message in case it doesn't want to
 * send a message and it should reply with a it's message in encrypted form if
 * a message has been queued.
 */
class TransmissionObserver(handler: TCPHandler, networkInfo: NetworkInfo) extends Observer[FruitarianMessage] {

  var messageRound: Promise[Boolean] = _
  val MESSAGE_ROUND_TIMEOUT = 2000
  val BACKOFF_RANGE = 10
  val messageQueue = new mutable.Queue[String]()
  var messageSent: String = ""
  var backoff = 0

  def queueMessage(message: String): Unit = {
    if (message.length > DCnet.MESSAGE_SIZE) {
      throw new Exception("Message too long.")
    }
    messageQueue.enqueue(message)
  }

  /**
   * Starts a message round. The node calling this function is expected to be
   * elected the center node.
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
    assert(networkInfo.center, "Starting message round while not being " +
      "the center node.")
    // TODO: Add identifier to each round, such that returned TransmitMessages
    //        can be verified to be of the correct round.
    if (networkInfo.center) {
      // Send a TransmitRequest to all peers and itself (as this node is also part of the clique).
      sendMessageToClique((address: Address) => TransmitRequest(networkInfo.ownAddress, address))

      // Set the amount of requests sent.
      DCnet.transmitRequestsSent = networkInfo.cliquePeers.length + 1

      println(s"[S] Started Message round for ${DCnet.transmitRequestsSent} node(s).")

      import scala.concurrent.ExecutionContext.Implicits.global
      messageRound = Promise[Boolean]()
      Future {
        // TODO: Find a better way to sleep, this is causing the logs to be somewhat delayed.
        Thread.sleep(MESSAGE_ROUND_TIMEOUT)
        if (!messageRound.isCompleted) {
          messageRound failure (_)
          println("[S] Message round timed out, retrying...")

          // Clear possible remaining responses.
          DCnet.clearResponses()

          // Send a "TIMEOUT" Text message to all peers to let them know the
          // round failed and trigger the message requeue behaviour if one of
          // them actually sent a message this round.
          sendMessageToClique((address: Address) => TextMessage(networkInfo.ownAddress, address, "TIMEOUT"))

          startMessageRound()
        }
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


  override def receiveUpdate(event: FruitarianMessage): Unit = event match {
    case TransmitRequest(from, to) =>
      if (messageQueue.nonEmpty && messageSent.isEmpty && backoff == 0) {
        // If we have a message to send and are not waiting for confirmation
        // of a previous message, send the next message. If we failed to send
        // a message and have a backoff we have to wait this cycle.
        // TODO: This 'just-send-it' behaviour can cause collisions, as
        //  multiple nodes could send a message at the same time. It wil also
        //  produce nonsense messages in case no one sends an actual encrypted
        //  message.
        messageSent = messageQueue.dequeue()
        handler.sendMessage(TransmitMessage(to, from, DCnet.encryptMessage(messageSent, networkInfo.cliquePeers.toList)))
      } else {
        // Else send a random message.
        handler.sendMessage(TransmitMessage(to, from, DCnet.getRandom(networkInfo.cliquePeers.toList)))
      }
      // Decrease the backoff by one until 0.
      backoff = math.max(0, backoff - 1)

    case TransmitMessage(_, _, message) =>
      DCnet.responses += message
      if (DCnet.canDecrypt) {
        // Complete the messageRound promise to avoid the timeout call.
        messageRound complete Try(true)

        // Send the decrypted message to the clique.
        val decryptedMessage = DCnet.decryptReceivedMessages()
        sendMessageToClique((address: Address) => TextMessage(networkInfo.ownAddress, address, decryptedMessage))

        // Since we do not have leader election yet, keep the message rounds
        // going with this node as centre node. A delay of 5000 is set between
        // rounds for testing purposes.
        Thread.sleep(5000)
        startMessageRound()
      }

    case TextMessage(_, _, msg) if !messageSent.isEmpty =>
      // If we recently sent a message, the next TextMessage received should be
      // this message. If not we need to resend the message.
      if (msg != messageSent) {
        // If the message is not as sent, queue the message for sending again.
        // We apply a random backoff in amount of cycles to hopefully prevent
        // another collision.
        queueMessage(messageSent)
        backoff = new Random().nextInt(BACKOFF_RANGE)
        println(s"[C] Message not sent correctly, enqueued again in $backoff cycles.")
      }
      // Unblock the message sending process to allow the next message or a resend.
      messageSent = ""


    case _ =>
  }
}
