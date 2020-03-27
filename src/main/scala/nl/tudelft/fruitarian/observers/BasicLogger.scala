package nl.tudelft.fruitarian.observers

import nl.tudelft.fruitarian.p2p.messages.{FruitarianMessage, NextRoundMessage, ResultMessage, TextMessage, TransmitMessage, TransmitRequest}
import nl.tudelft.fruitarian.patterns.Observer

/* Example Observer that logs all incoming messages. */
object BasicLogger extends Observer[FruitarianMessage] {

  def stripNonReadableBytes(message: String): String = {
    message.replaceAll("[^ -~]", "")
  }

  def receiveUpdate(event: FruitarianMessage): Unit = event match {
    case TextMessage(from, _, message) => stripNonReadableBytes(message) match {
      case s if s.nonEmpty => println(s"[${from.socket}][TEXT]: $message")
      case _ =>
    }
    case ResultMessage(from, _, message) => stripNonReadableBytes(message) match {
      case s if s.nonEmpty => println(s"[${from.socket}][RESULT]: $message")
      case _ =>
    }
    case TransmitRequest(from, _, roundId) => println(s"[${from.socket}][R$roundId][MESSAGE_REQUEST]")
    case TransmitMessage(from, _, (roundId, message)) => println(s"[${from.socket}][R$roundId][BYTE][${message.length}]: 0x${message.map("%02X" format _).mkString.take(40)}...[truncated]")
    case NextRoundMessage(from, _, roundId) => println(s"[${from.socket}] NEXT ROUND R${roundId}")
    case _ => println(event)
  }
}
