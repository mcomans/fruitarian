package nl.tudelft.fruitarian.observers

import nl.tudelft.fruitarian.p2p.messages.{FruitarianMessage, TextMessage, TransmitMessage, TransmitRequest}
import nl.tudelft.fruitarian.patterns.Observer

/* Example Observer that logs all incoming messages. */
object BasicLogger extends Observer[FruitarianMessage] {
  def receiveUpdate(event: FruitarianMessage): Unit = event match {
    case TextMessage(from, _, message) => println(s"[${from.socket}][TEXT]: $message")
    case TransmitRequest(from, _) => println(s"[${from.socket}][MESSAGE_REQUEST]")
    case TransmitMessage(from, _, message) => println(s"[${from.socket}][BYTE][${message.length}]: ${message.map("%02X" format _).mkString("utf-8").take(40)}...[truncated]")
    case _ => println(event)
  }
}
