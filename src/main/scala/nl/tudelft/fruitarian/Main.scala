package nl.tudelft.fruitarian

import java.net.InetSocketAddress

import nl.tudelft.fruitarian.models.NetworkInfo
import nl.tudelft.fruitarian.observers._
import nl.tudelft.fruitarian.p2p.messages.EntryRequest
import nl.tudelft.fruitarian.p2p.{Address, TCPHandler}

object Main extends App {
  /* This example will start a Transmission Message Round with itself. */
  val networkInfo = new NetworkInfo()

  val handler = if (args.length == 0) new TCPHandler() else new TCPHandler(args(0).toInt)
  networkInfo.ownAddress = Address(handler.serverHost)
//  handler.addMessageObserver(BasicLogger)
  handler.addMessageObserver(new Greeter(handler))
  handler.addMessageObserver(new EntryObserver(handler, networkInfo))
  var transmissionObserver = new TransmissionObserver(handler, networkInfo)
  handler.addMessageObserver(transmissionObserver)
//    var experimentObserver = new ExperimentObserver(handler, transmissionObserver)
//    handler.addMessageObserver(experimentObserver)

  Thread.sleep(1000)

  //transmissionObserver.queueMessage(s"Hi there from ${networkInfo.ownAddress
  //  .socket.getPort}")

  if (args.length == 0) {
    val utilizationSenderObserver = new UtilizationObserver(handler, transmissionObserver)
    handler.addMessageObserver(utilizationSenderObserver)
    // Start first round as first node
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
