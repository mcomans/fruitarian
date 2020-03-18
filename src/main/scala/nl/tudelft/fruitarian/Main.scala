package nl.tudelft.fruitarian

import nl.tudelft.fruitarian.observers.{BasicLogger, Greeter}
import nl.tudelft.fruitarian.p2p.messages.TextMessage
import nl.tudelft.fruitarian.p2p.{Address, TCPHandler}

object Main extends App {
  /* This example will log all the messages that are received, either by the
  client or server part of the application and greet new clients that connect
  to the server with the message "Hello World". */

  val handler = new TCPHandler()
  handler.addMessageObserver(BasicLogger)
  handler.addMessageObserver(new Greeter(handler))

  val helloWorldMessage = TextMessage(
    Address(handler.serverHost),
    Address(handler.serverHost),
    "Hello World")

  handler.sendMessage(helloWorldMessage)

  Thread.sleep(5000)
  handler.shutdown()
}
