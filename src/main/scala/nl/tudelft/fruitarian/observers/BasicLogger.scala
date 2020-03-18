package nl.tudelft.fruitarian.observers

import nl.tudelft.fruitarian.p2p.messages.{FruitarianMessage, TextMessage}
import nl.tudelft.fruitarian.patterns.Observer

/* Example Observer that logs all incoming messages. */
object BasicLogger extends Observer[FruitarianMessage] {
  def receiveUpdate(event: FruitarianMessage): Unit = event match {
    case TextMessage(from, _, message) => println(s"[${from.socket}][TEXT]: $message")
  }
}
