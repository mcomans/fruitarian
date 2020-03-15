package nl.tudelft.fruitarian

import nl.tudelft.fruitarian.p2p.{Address, Msg, MsgHeader, MsgType,
  TCPHandler}
import nl.tudelft.fruitarian.patterns.Observer

object Main extends App {
  val handler = new TCPHandler()

  object BasicLogger extends Observer[Msg] {
    def receiveUpdate(event: Msg): Unit = event match {
      case Msg(MsgHeader(msgType, _, _), text) if msgType == MsgType.Text =>
        println("TEXT: " + text.map(_.toChar).mkString)
      case Msg(MsgHeader(msgType, _, _), body) if msgType == MsgType.Bytes =>
        println("BYTES: " + body)
    }
  }

  handler.addMessageObserver(BasicLogger)

  val helloWorldMessage = Msg(MsgHeader(
    MsgType.Text,
    Address(handler.serverHost),
    Address(handler.serverHost)),
    body = "Hello World".getBytes())

  handler.sendMessage(helloWorldMessage)

  Thread.sleep(5000)
  handler.shutdown()
}
