package nl.tudelft.fruitarian.p2p.messages

import org.json4s.{DefaultFormats, Formats}

abstract class FruitarianMessage(val header: MessageHeader) {
  implicit val formats: Formats = DefaultFormats
  def serializeBody(): String
}