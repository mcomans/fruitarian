package nl.tudelft.fruitarian

import nl.tudelft.fruitarian.observers.{BasicLogger, Greeter}
import nl.tudelft.fruitarian.p2p.{Address, Msg, MsgHeader, MsgType, TCPHandler}

object Main extends App {
  val handler = new TCPHandler()
  handler.addMessageObserver(BasicLogger)
  handler.addMessageObserver(new Greeter(handler))

  val helloWorldMessage = Msg(MsgHeader(
    MsgType.Text,
    Address(handler.serverHost),
    Address(handler.serverHost)),
    body = "Hello World".getBytes())

  handler.sendMessage(helloWorldMessage)

  Thread.sleep(5000)
  handler.shutdown()
}
