package nl.tudelft.fruitarian.p2p.messages

import nl.tudelft.fruitarian.p2p.{Address, MessageSerializer}
import org.json4s.Formats

case class SeedMessage(from: Address, to: Address, seed: String) extends FruitarianMessage(MessageHeader(SeedMessage.MessageType, from, to)) {
	override def serializeBody(): String = seed
}

case object SeedMessage {
	val MessageType = "SEED_MESSAGE"
	implicit val formats: Formats = MessageSerializer.messageSerializeFormats

	def fromHeaderAndBody(header: MessageHeader, body: String): SeedMessage = {
		SeedMessage(header.from, header.to, body)
	}
}
