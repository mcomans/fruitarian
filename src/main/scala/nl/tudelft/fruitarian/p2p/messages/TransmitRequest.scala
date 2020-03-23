package nl.tudelft.fruitarian.p2p.messages

import nl.tudelft.fruitarian.p2p.Address

case class TransmitRequest(from: Address, to: Address) extends FruitarianMessage(MessageHeader(TransmitRequest.MessageType, from, to)) {
	override def serializeBody(): String = ""
}

case object TransmitRequest {
	val MessageType = "TRANSMIT_REQUEST"
}