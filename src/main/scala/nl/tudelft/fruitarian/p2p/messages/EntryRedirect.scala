package nl.tudelft.fruitarian.p2p.messages

import nl.tudelft.fruitarian.p2p.{Address, MessageSerializer}
import org.json4s.Formats
import org.json4s.jackson.Serialization.{read, write}

case class EntryRedirect(from: Address, to: Address, redirectAdress: Address) extends FruitarianMessage(MessageHeader(EntryRedirect.MessageType, from, to)) {
	override def serializeBody(): String = {
		write(redirectAdress)
	}
}

case object EntryRedirect {
	val MessageType = "ENTRY_REDIRECT"
	implicit val formats: Formats = MessageSerializer.messageSerializeFormats

	def fromHeaderAndBody(header: MessageHeader, body: String): FruitarianMessage = {
		EntryRedirect(header.from, header.to, read[Address](body))
	}
}
