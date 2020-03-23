package nl.tudelft.fruitarian.p2p

import java.net.InetSocketAddress

import nl.tudelft.fruitarian.p2p.messages._
import org.json4s._
import org.json4s.jackson.JsonMethods._

// Simple case class serialisation with help of:
// https://commitlogs.com/2017/01/14/serialize-deserialize-json-with-json4s-in-scala/

/** Simple Helper to serialize and deserialize messages. */
object MessageSerializer {
  val messageSerializeFormats: Formats = DefaultFormats + InetSocketAddressSerializer
  implicit val formats: Formats = messageSerializeFormats

  def serializeMsg(msg: FruitarianMessage): String = {
    compact(render(JObject(("header", Extraction.decompose(msg.header)), ("body", JString(msg.serializeBody())))))
  }
  def deserialize(data: String): FruitarianMessage = {
    parse(data) match {
      case JObject(("header", h) :: ("body", JString(body)) :: Nil) => h.extract[MessageHeader] match {
        case header @ MessageHeader(TextMessage.MessageType, _, _) => TextMessage.fromHeaderAndBody(header, body)
        case header @ MessageHeader(EntryResponse.MessageType, _, _) => EntryResponse.fromHeaderAndBody(header, body)
        case MessageHeader(EntryRequest.MessageType, from, to) => EntryRequest(from, to)
        case MessageHeader(TransmitRequest.MessageType, from, to) => TransmitRequest(from, to)
        case header @ MessageHeader(AnnounceMessage.MessageType, _, _) => AnnounceMessage.fromHeaderAndBody(header, body)
        case header @ MessageHeader(TransmitMessage.MessageType, _, _) => TransmitMessage.fromHeaderAndBody(header, body)
      }
    }
  }
}

case object InetSocketAddressSerializer extends
  CustomSerializer[InetSocketAddress](format => ({
    case JObject(List(
      JField("addr", JString(addr)),
      JField("port", JString(port))
    )) => new InetSocketAddress(addr, port.toInt)
  }, {
    case socket: InetSocketAddress => JObject(List(
      JField("addr", JString(socket.getHostString)),
      JField("port", JString(socket.getPort.toString))
    ))
  }))
