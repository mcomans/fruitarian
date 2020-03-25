package nl.tudelft.fruitarian.observers

import nl.tudelft.fruitarian.models.{DCnet, NetworkInfo}
import nl.tudelft.fruitarian.p2p.TCPHandler
import nl.tudelft.fruitarian.p2p.messages.{AnnounceMessage, FruitarianMessage, TransmitMessage, TransmitRequest}
import nl.tudelft.fruitarian.patterns.Observer

import scala.collection.mutable

/**
 * This class handles the Transmission message phase. This means that for each
 * 'round' it should reply with a random message in case it doesn't want to
 * send a message and it should reply with a it's message in encrypted form if
 * a message has been queued.
 */
class TransmissionObserver(handler: TCPHandler, networkInfo: NetworkInfo) extends Observer[FruitarianMessage] {
  val messageQueue = new mutable.Queue[String]()

  def queueMessage(message: String): Unit = {
    if (message.length > DCnet.MESSAGE_SIZE) {
      throw new Exception("Message too long.")
    }
    messageQueue.enqueue(message)
  }

  override def receiveUpdate(event: FruitarianMessage): Unit = event match {
    case TransmitRequest(from, to) =>
      // Todo: replace this when message transfer is implemented.
      // Send message back to the center node with random xor value.
      handler.sendMessage(TransmitMessage(to, from, DCnet.getRandom(networkInfo.cliquePeers.toList)))

    case TransmitMessage(from, to, message) =>
      networkInfo.responses += message
      // Todo: replace this when message transfer is implemented.
      // When the center node has three responses, encrypt a message based on
      // the peers and decrypt it using the responses.
      if (networkInfo.responses.length == 3) {
        var encryptedMessage = DCnet.encryptMessage("Hi there!", networkInfo.cliquePeers.toList)
        var decryptedMessage = DCnet.decryptMessage((networkInfo.responses += encryptedMessage).toList)
        println("RESPONSE: " + decryptedMessage)
      }

    case AnnounceMessage(from, to, seed) =>
      // Todo: replace this when message transfer is implemented.
      // Center node sends request when 3 other peers have entered the network.
      // The request asks all nodes to send their random xor values.
      if (networkInfo.center && networkInfo.cliquePeers.length == 3) {
        networkInfo.cliquePeers.foreach(p => {
          handler.sendMessage(TransmitRequest(to, p.address))
        })
      }
  }
}
