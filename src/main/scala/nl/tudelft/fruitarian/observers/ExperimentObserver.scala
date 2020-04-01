package nl.tudelft.fruitarian.observers

import nl.tudelft.fruitarian.models.DCnet
import nl.tudelft.fruitarian.p2p.TCPHandler
import nl.tudelft.fruitarian.p2p.messages.{FruitarianMessage, ResultMessage}
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

  val firstRoundAt: Long = System.currentTimeMillis()
  var failedRoundsSeen: Int = 0
  var lastFailedRound: Int = -1

  val characters = "abcdefghijklmnopqrstuvwxyz".split("")

  def generateRandomMessage(msgSize: Int): String = {
    var msg = ""
    for (x <- 1 to msgSize) {
      msg += characters(random.nextInt(characters.length))
    }
    msg
  }

  def sendNewMessage(): Unit = {
    lastMessage = generateRandomMessage(DCnet.MESSAGE_SIZE)
    transmissionObserver.queueMessage(lastMessage)
    messageSentAt = System.currentTimeMillis()
    messagesSent += 1
  }

  def calculateDelay(): Unit = {
    val delay = System.currentTimeMillis() - messageSentAt
    delays += delay
    val avgDelay = delays.sum / messagesSent
    println(s"[TEST][$messagesSent/$noMessages] Last delay: ${delay}ms | Avg delay: ${avgDelay}ms")
  }

  def calculateBandwidth(): Unit = {
    val rounds = transmissionObserver.roundId
    val timeDiff = System.currentTimeMillis() - firstRoundAt
    val correctRounds = rounds - failedRoundsSeen

    if (correctRounds <= 0 || rounds <= 0) {
      return
    }

    val avgTimePerRound: Double = timeDiff / rounds
    val avgTimePerRoundCorrected: Double = timeDiff / correctRounds
    val theoreticalMaxBandwidth: Double = 1000 / avgTimePerRound * DCnet.MESSAGE_SIZE
    val actualMaxBandwidth: Double = 1000 / avgTimePerRoundCorrected * DCnet.MESSAGE_SIZE
    val prettyTheoreticalMaxBandwidth = (math rint theoreticalMaxBandwidth * 8) / 1024
    val prettyActualMaxBandwidth = (math rint actualMaxBandwidth * 8) / 1024

    println(s"[TEST][$messagesSent/$noMessages] Avg time per round: ${avgTimePerRound}ms" +
      s" | Theoretical max bandwidth: ${prettyTheoreticalMaxBandwidth} Kb/s" +
      s" | Actual max bandwidth: ${prettyActualMaxBandwidth} Kb/s")
  }

  /* Start experiment */
  sendNewMessage()

  override def receiveUpdate(event: FruitarianMessage): Unit = event match {
    case ResultMessage(_, _, message) if message == lastMessage =>
      calculateDelay()
      calculateBandwidth()
      if (messagesSent < noMessages) {
        sendNewMessage()
      }
    case ResultMessage(_, _, message) if message == "TIMEOUT" =>
      if (transmissionObserver.roundId > lastFailedRound) {
        lastFailedRound = transmissionObserver.roundId
        failedRoundsSeen += 1
      }
    case _ =>
  }
}
