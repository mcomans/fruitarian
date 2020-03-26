package nl.tudelft.fruitarian.observers

import nl.tudelft.fruitarian.p2p.messages.{FruitarianMessage, TextMessage, TransmitMessage, TransmitRequest}
import nl.tudelft.fruitarian.patterns.Observer

/* Example Observer that logs all incoming messages. */
object BasicLogger extends Observer[FruitarianMessage] {
  def receiveUpdate(event: FruitarianMessage): Unit = event match {
    case TextMessage(from, _, message) => println(s"[${from.socket}][TEXT]: $message")
    case TransmitRequest(from, _, roundId) => println(s"[${from.socket}][R$roundId][MESSAGE_REQUEST]")
    case TransmitMessage(from, _, (roundId, message)) => println(s"[${from.socket}][R$roundId][BYTE][${message.length}]: ${message.map("%02X" format _).mkString("utf-8").take(40)}...[truncated]")
    case _ => println(event)
  }
}
