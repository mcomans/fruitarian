package nl.tudelft.fruitarian

import java.net.InetSocketAddress

import nl.tudelft.fruitarian.models.NetworkInfo
import nl.tudelft.fruitarian.observers.{BasicLogger, EntryObserver, Greeter, TransmissionObserver}
import nl.tudelft.fruitarian.p2p.messages.EntryRequest
import nl.tudelft.fruitarian.p2p.{Address, TCPHandler}

object Main extends App {
  /* This example will start a Transmission Message Round with itself. */

  //TODO: remove when center node election is implemented.
  val networkInfo = new NetworkInfo()

  val handler = if (args.length == 0) new TCPHandler() else new TCPHandler(args(0).toInt)
  networkInfo.ownAddress = Address(handler.serverHost)
  handler.addMessageObserver(BasicLogger)
  handler.addMessageObserver(new Greeter(handler))
  handler.addMessageObserver(new EntryObserver(handler, networkInfo))
  var transmissionObserver = new TransmissionObserver(handler, networkInfo)
  handler.addMessageObserver(transmissionObserver)

  Thread.sleep(1000)

  transmissionObserver.queueMessage(s"Hi there from ${networkInfo.ownAddress.socket.getPort}")

  // If we are a the centre node.
  if (args.length == 0) {
    // TODO: Actually start a message round once elected centre node.
    transmissionObserver.startMessageRound()
  }

  // If we are a client node.
  if (args.length > 0) {
    val helloWorldMessage = EntryRequest(
      Address(handler.serverHost),
      Address(new InetSocketAddress(args(1), args(2).toInt)),
      networkInfo.nodeId)

    handler.sendMessage(helloWorldMessage)
  }

  sys.addShutdownHook(() => {
    handler.shutdown()
  })
}
