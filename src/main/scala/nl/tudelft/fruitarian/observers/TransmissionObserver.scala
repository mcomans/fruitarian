package nl.tudelft.fruitarian.observers

import nl.tudelft.fruitarian.models.{DCnet, NetworkInfo}
import nl.tudelft.fruitarian.p2p.TCPHandler
import nl.tudelft.fruitarian.p2p.messages.{FruitarianMessage, TransmitMessage, TransmitRequest}
import nl.tudelft.fruitarian.patterns.Observer

import scala.collection.mutable
import scala.concurrent.{Future, Promise}

/**
 * This class handles the Transmission message phase. This means that for each
 * 'round' it should reply with a random message in case it doesn't want to
 * send a message and it should reply with a it's message in encrypted form if
 * a message has been queued.
 */
class TransmissionObserver(handler: TCPHandler, networkInfo: NetworkInfo) extends Observer[FruitarianMessage] {

  var messageRound: Promise[_] = _
  val MESSAGE_ROUND_TIMEOUT = 2000;
  val messageQueue = new mutable.Queue[String]()

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
      // Send a TransmitRequest to all peers.
      networkInfo.cliquePeers.foreach(p => {
        handler.sendMessage(TransmitRequest(networkInfo.ownAddress, p.address))
      })
      // Also send one to the master node itself.
      handler.sendMessage(TransmitRequest(networkInfo.ownAddress, networkInfo.ownAddress))

      // Set the amount of requests sent.
      DCnet.transmitRequestsSent = networkInfo.cliquePeers.length + 1

      println(s"[S] Started Message round for ${DCnet.transmitRequestsSent} node(s).")

      import scala.concurrent.ExecutionContext.Implicits.global
      messageRound = Promise[Boolean]()
      Future {
        Thread.sleep(MESSAGE_ROUND_TIMEOUT)
        if (!messageRound.isCompleted) {
          messageRound failure (_)
          println("[S] Message round timed out, retrying...")
          startMessageRound()
        }
      }
    }
  }


  override def receiveUpdate(event: FruitarianMessage): Unit = event match {
    case TransmitRequest(from, to) =>
      if (messageQueue.nonEmpty) {
        // If we have a message to send, send it.
        // TODO: This 'just-send-it' behaviour can cause collisions, as
        //  multiple nodes could send a message at the same time. It wil also
        //  produce nonsense messages in case no one sends an actual encrypted
        //  message.
        //handler.sendMessage(TransmitMessage(to, from, DCnet.encryptMessage(messageQueue.dequeue(), networkInfo.cliquePeers.toList)))
      } else {
        // Else send a random message.
        //handler.sendMessage(TransmitMessage(to, from, DCnet.getRandom(networkInfo.cliquePeers.toList)))
      }

    case TransmitMessage(_, _, message) =>
      DCnet.responses += message
      if (DCnet.canDecrypt) {
        messageRound complete (_)
        val decryptedMessage = DCnet.decryptReceivedMessages()
        println(s"[S] Round completed, message[${decryptedMessage.length}]: $decryptedMessage")

        // Since we do not have leader election yet, keep the message rounds
        // going with this node as centre node. A delay of 5000 is set between
        // rounds for testing purposes.
        Thread.sleep(5000)
        startMessageRound()
      }

    case _ =>
  }
}
