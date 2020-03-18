package nl.tudelft.fruitarian.p2p.messages

import nl.tudelft.fruitarian.p2p.Address

case class AnnounceMessage(from: Address, to: Address) extends FruitarianMessage(MessageHeader(AnnounceMessage.MessageType, from, to)) {
  override def serializeBody(): String = ""
}

case object AnnounceMessage {
  val MessageType = "ANNOUNCE"
}