package nl.tudelft.fruitarian.observers

import nl.tudelft.fruitarian.p2p.TCPHandler
import nl.tudelft.fruitarian.p2p.messages.{FruitarianMessage, NextRoundMessage, ResultMessage}
import nl.tudelft.fruitarian.patterns.Observer

import scala.collection.mutable.ListBuffer
import scala.util.Random

class ExperimentObserver(handler: TCPHandler, transmissionObserver: TransmissionObserver) extends
  Observer[FruitarianMessage] {
  var messagesSent = 0
  val noMessages = 1000

  var delays = new ListBuffer[Long]()
  val random = new Random()
  var lastMessage = ""
  var messageSentAt = System.currentTimeMillis()

  var firstCenterRoundId: Int = -1
  var firstCenterRoundAt: Long = 0

  val characters = "abcdefghijklmnopqrstuvwxyz".split("")

  def generateRandomMessage(msgSize: Int): String = {
    var msg = ""
    for (x <- 1 to msgSize) {
      msg += characters(random.nextInt(characters.length))
    }
    msg
  }

  def sendNewMessage() {
    val delay = System.currentTimeMillis() - messageSentAt
    delays += delay
    println(s"[TEST] Last delay: ${delay}ms")
    lastMessage = generateRandomMessage(100)
    transmissionObserver.queueMessage(lastMessage)
    messageSentAt = System.currentTimeMillis()
    messagesSent += 1
  }

  def handleCenterRound(round: Int): Unit = {
    if (firstCenterRoundId < 0) {
      firstCenterRoundId = round
      firstCenterRoundAt = System.currentTimeMillis()
    } else {
      val roundDiff = round - firstCenterRoundId
      val timeDiff = System.currentTimeMillis() - firstCenterRoundAt
      val avgTimePerRound = timeDiff / roundDiff
      println(s"[TEST] Average time per round: ${avgTimePerRound}ms")
    }
  }

  /* Start experiment */
  sendNewMessage()

  override def receiveUpdate(event: FruitarianMessage): Unit = event match {
    case ResultMessage(from, to, message) if message == lastMessage =>
      if (messagesSent < noMessages) {
        sendNewMessage()
      }
      println(s"[TEST][$messagesSent/$noMessages] ${delays.sum / messagesSent}ms")
    case NextRoundMessage(_, _, roundId) if roundId > firstCenterRoundId =>
      handleCenterRound(roundId)
    case _ =>
  }
}
