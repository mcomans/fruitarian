package nl.tudelft.fruitarian

import java.net.InetSocketAddress

import nl.tudelft.fruitarian.models.NetworkInfo
import nl.tudelft.fruitarian.observers.{BasicLogger, EntryObserver, Greeter}
import nl.tudelft.fruitarian.p2p.messages.{EntryRequest, TextMessage}
import nl.tudelft.fruitarian.p2p.{Address, TCPHandler}

object Main extends App {
  /* This example will log all the messages that are received, either by the
  client or server part of the application and greet new clients that connect
  to the server with the message "Hello World". */
  val networkInfo = new NetworkInfo()

  val handler = if (args.length > 0) new TCPHandler(args(0).toInt) else new TCPHandler()
  handler.addMessageObserver(BasicLogger)
  handler.addMessageObserver(new Greeter(handler))
  handler.addMessageObserver(new EntryObserver(handler, networkInfo))

  if (args.length == 3) {
    val helloWorldMessage = EntryRequest(
      Address(handler.serverHost),
      Address(new InetSocketAddress(args(1), args(2).toInt)))

    handler.sendMessage(helloWorldMessage)
  }

  sys.addShutdownHook(() => {
    handler.shutdown()
  })


}
