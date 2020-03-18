package nl.tudelft.fruitarian.p2p

import java.net.InetSocketAddress

import nl.tudelft.fruitarian.p2p.messages._
import org.json4s._
import org.json4s.jackson.JsonMethods._

// Simple case class serialisation with help of:
// https://commitlogs.com/2017/01/14/serialize-deserialize-json-with-json4s-in-scala/

/** Simple Helper to serialize and deserialize messages. */
object MessageSerializer {
  implicit val formats: Formats = DefaultFormats + InetSocketAddressSerializer

  def serializeMsg(msg: FruitarianMessage): String = {
    compact(render(JObject(("header", Extraction.decompose(msg.header)), ("body", JString(msg.serializeBody())))))
  }
  def deserialize(data: String): FruitarianMessage = {
    parse(data) match {
      case JObject(("header", h) :: ("body", JString(body)) :: Nil) => h.extract[MessageHeader] match {
        case header @ MessageHeader(TextMessage.MessageType, _, _) => TextMessage.fromHeaderAndBody(header, body)
        case header @ MessageHeader(EntryResponse.MessageType, _, _) => EntryResponse.fromHeaderAndBody(header, body)
        case MessageHeader(EntryRequest.MessageType, from, to) => EntryRequest(from, to)
        case MessageHeader(AnnounceMessage.MessageType, from, to) => AnnounceMessage(from, to)
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
