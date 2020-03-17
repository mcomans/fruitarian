package nl.tudelft.fruitarian.observers

import nl.tudelft.fruitarian.p2p.{Msg, MsgHeader, MsgType}
import nl.tudelft.fruitarian.patterns.Observer

/* Example Observer that logs all incoming messages. */
object BasicLogger extends Observer[Msg] {
  def receiveUpdate(event: Msg): Unit = event match {
    case Msg(MsgHeader(msgType, from, _), text) if msgType == MsgType.Text =>
      val msg = text.map(_.toChar).mkString
      println(s"[${from.socket}][TEXT]: $msg")
    case Msg(MsgHeader(msgType, from, _), body) if msgType == MsgType.Bytes =>
      println(s"[${from.socket}][BYTES]: $body")
  }
}
