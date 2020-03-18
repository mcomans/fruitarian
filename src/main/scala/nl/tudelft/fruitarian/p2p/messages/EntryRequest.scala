package nl.tudelft.fruitarian.p2p.messages

import nl.tudelft.fruitarian.p2p.Address

case class EntryRequest(from: Address, to: Address) extends FruitarianMessage(MessageHeader(EntryRequest.MessageType, from, to)) {
  override def serializeBody(): String = ""
}

case object EntryRequest {
  val MessageType = "ENTRY_REQUEST"
}