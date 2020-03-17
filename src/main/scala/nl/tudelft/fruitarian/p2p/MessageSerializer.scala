package nl.tudelft.fruitarian.p2p

import java.net.InetSocketAddress

import org.json4s._
import org.json4s.jackson.Serialization.{read, write}

// Simple case class serialisation with help of:
// https://commitlogs.com/2017/01/14/serialize-deserialize-json-with-json4s-in-scala/

/** Simple Helper to serialize and deserialize messages. */
object MessageSerializer {
  implicit val formats: Formats = DefaultFormats + MsgTypeSerializer + InetSocketAddressSerializer

  def serializeMsg(msg: Msg): String = write(msg)
  def deserialize(data: String): Msg = {
    read[Msg](data)
  }
}

case object MsgTypeSerializer extends CustomSerializer[MsgType.Value](format => ( {
  case JString("BYTES") => MsgType.Bytes
  case JString("TEXT") => MsgType.Text
}, {
  case MsgType.Bytes => JString("BYTES")
  case MsgType.Text => JString("TEXT")
}))

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
