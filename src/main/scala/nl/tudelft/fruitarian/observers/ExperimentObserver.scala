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
  var failedRoundsSeen: Int = 0

  val characters = "abcdefghijklmnopqrstuvwxyz".split("")

  def generateRandomMessage(msgSize: Int): String = {
    var msg = ""
    for (x <- 1 to msgSize) {
      msg += characters(random.nextInt(characters.length))
    }
    msg
  }

  def sendNewMessage(): Unit = {
    lastMessage = generateRandomMessage(100)
    transmissionObserver.queueMessage(lastMessage)
    messageSentAt = System.currentTimeMillis()
    messagesSent += 1
  }

  def calculateDelay(): Unit = {
    val delay = System.currentTimeMillis() - messageSentAt
    delays += delay
    val avgDelay = delays.sum / (messagesSent - failedRoundsSeen)
    println(s"[TEST][$messagesSent/$noMessages] Last delay: ${delay}ms | Avg delay: ${avgDelay}ms")
  }

  def handleCenterRound(round: Int): Unit = {
    if (firstCenterRoundId < 0) {
      firstCenterRoundId = round
      failedRoundsSeen = 0
      firstCenterRoundAt = System.currentTimeMillis()
    } else {
      val roundDiff: Int = round - firstCenterRoundId
      val timeDiff: Long = System.currentTimeMillis() - firstCenterRoundAt
      val correctRounds: Int = roundDiff - failedRoundsSeen

      if (correctRounds <= 0 || roundDiff <= 0) {
        return
      }

      val avgTimePerRound: Long = timeDiff / roundDiff
      val avgTimePerRoundCorrected: Long = timeDiff / correctRounds
      val theoreticalMaxBandwidth: Long = 1000 / avgTimePerRound * 280
      val actualMaxBandwidth: Long = 1000 / avgTimePerRoundCorrected * 280

      println(s"[TEST][$messagesSent/$noMessages] Avg time per round: ${avgTimePerRound}ms" +
        s" | Theoretical max bandwidth: ${theoreticalMaxBandwidth / 8} b/s" +
        s" | Actual max bandwidth: ${actualMaxBandwidth / 8} b/s")
    }
  }

  /* Start experiment */
  sendNewMessage()

  override def receiveUpdate(event: FruitarianMessage): Unit = event match {
    case ResultMessage(from, _, message) =>
      if (message == lastMessage) {
        calculateDelay()
      } else if (message == "TIMEOUT") {
        failedRoundsSeen += 1
      }
      if (messagesSent < noMessages) {
        sendNewMessage()
      }
    case NextRoundMessage(_, _, roundId) if roundId > firstCenterRoundId =>
      handleCenterRound(roundId)
    case _ =>
  }
}
