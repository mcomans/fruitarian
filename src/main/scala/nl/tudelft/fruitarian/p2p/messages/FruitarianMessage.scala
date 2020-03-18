package nl.tudelft.fruitarian.p2p.messages

import nl.tudelft.fruitarian.p2p.InetSocketAddressSerializer
import org.json4s.{DefaultFormats, Formats}

abstract class FruitarianMessage(val header: MessageHeader) {
  implicit val formats: Formats = DefaultFormats + InetSocketAddressSerializer
  def serializeBody(): String
}