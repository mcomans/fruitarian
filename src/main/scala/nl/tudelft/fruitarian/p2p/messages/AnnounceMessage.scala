package nl.tudelft.fruitarian.p2p.messages

import nl.tudelft.fruitarian.p2p.{Address, MessageSerializer}
import org.json4s.Formats

case class AnnounceMessage(from: Address, to: Address, seed: String) extends FruitarianMessage(MessageHeader(AnnounceMessage.MessageType, from, to)) {
	override def serializeBody(): String = seed
}

case object AnnounceMessage {
	val MessageType = "ANNOUNCE_MESSAGE"
	implicit val formats: Formats = MessageSerializer.messageSerializeFormats

	def fromHeaderAndBody(header: MessageHeader, body: String): AnnounceMessage = {
		AnnounceMessage(header.from, header.to, body)
	}
}
