package nl.tudelft.fruitarian.observers

import nl.tudelft.fruitarian.p2p.messages.{FruitarianMessage, ResultMessage}
import nl.tudelft.fruitarian.p2p.{MessageSerializer, TCPHandler}
import nl.tudelft.fruitarian.patterns.Observer

import scala.util.Random

class UtilizationObserver(handler: TCPHandler, transmissionObserver: TransmissionObserver) extends
  Observer[FruitarianMessage] {
  var messagesSent = 0
  val noMessages = 1000
  var totalBytesReceived = 0
  var messageBytesReceived = 0

  val random = new Random()
  var lastMessage = ""

  val characters = "abcdefghijklmnopqrstuvwxyz".split("")

  def generateRandomMessage(msgSize: Int): String = {
    var msg = ""
    for (x <- 1 to msgSize) {
      msg += characters(random.nextInt(characters.length))
    }
    msg
  }

  def sendNewMessage() {
    lastMessage = generateRandomMessage(4096)
    transmissionObserver.queueMessage(lastMessage)
    messagesSent += 1
  }

  /* Start experiment */
  sendNewMessage()

  override def receiveUpdate(event: FruitarianMessage): Unit = event match {
    case ResultMessage(from, to, message) if message == lastMessage =>
      if (messagesSent < noMessages) {
        sendNewMessage()
      }
      totalBytesReceived += MessageSerializer.serializeMsg(event).getBytes().length
      messageBytesReceived += message.getBytes().length
      println("[UTILIZATION_SENDER] messages sent: " + messagesSent
      + " total: " + totalBytesReceived
      + " message: " + messageBytesReceived
      + " avg: " + messageBytesReceived.toFloat / totalBytesReceived)
    case _ =>
      totalBytesReceived += MessageSerializer.serializeMsg(event).getBytes().length
  }
}
