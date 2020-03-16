package nl.tudelft.fruitarian.observers

import nl.tudelft.fruitarian.p2p.{Msg, MsgHeader, MsgType, TCPHandler}
import nl.tudelft.fruitarian.patterns.Observer

/* Example Observer that sends "Hi there!" upon receiving "Hello World" */
class Greeter(handler: TCPHandler) extends Observer[Msg] {
  def receiveUpdate(event: Msg): Unit = event match {
    case Msg(MsgHeader(MsgType.Text, from, to), body) =>
      if (body.map(_.toChar).mkString == "Hello World") {
        handler.sendMessage(Msg(MsgHeader(MsgType.Text, to, from), "Hi there!".getBytes))
      }
  }
}
