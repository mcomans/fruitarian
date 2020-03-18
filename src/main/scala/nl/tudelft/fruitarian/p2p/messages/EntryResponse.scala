package nl.tudelft.fruitarian.p2p.messages

import nl.tudelft.fruitarian.models.Peer
import nl.tudelft.fruitarian.p2p.{Address, MessageSerializer}
import org.json4s.jackson.Serialization.{read, write}
import org.json4s.Formats

case class EntryResponse(from: Address, to: Address, peerList: List[Peer]) extends FruitarianMessage(MessageHeader(EntryResponse.MessageType, from, to)) {
  override def serializeBody(): String = {
    write(peerList)
  }
}

case object EntryResponse {
  val MessageType = "ENTRY_RESPONSE"
  implicit val formats: Formats = MessageSerializer.messageSerializeFormats

  def fromHeaderAndBody(header: MessageHeader, body: String): FruitarianMessage = {
    EntryResponse(header.from, header.to, read[List[Peer]](body))
  }
}
