package nl.tudelft.fruitarian

import java.net.InetSocketAddress

import nl.tudelft.fruitarian.models.NetworkInfo
import nl.tudelft.fruitarian.observers.{BasicLogger, ChatLogger, EntryObserver, ExperimentObserver, Greeter, TransmissionObserver}
import nl.tudelft.fruitarian.p2p.messages.EntryRequest
import nl.tudelft.fruitarian.p2p.{Address, TCPHandler}

object Main extends App {
  // Define behaviour with program arguments.
  val experimentNode = args.contains("-e")
  val chatNode = args.contains("--chat")
  val experimentStartingNode = args.length == 1 && experimentNode
  val chatStartingNode = args.length == 1 && chatNode
  val startingNode = args.length == 0 || experimentStartingNode || chatStartingNode

  // Use port 5000 as default server port.
  val serverPort = if (startingNode) 5000 else args(0).toInt

  val networkInfo = new NetworkInfo(serverPort)
  val handler = new TCPHandler(serverPort)

  if (!chatNode) handler.addMessageObserver(BasicLogger)
  handler.addMessageObserver(new Greeter(handler))
  handler.addMessageObserver(new EntryObserver(handler, networkInfo))
  var transmissionObserver = new TransmissionObserver(handler, networkInfo)
  handler.addMessageObserver(transmissionObserver)

  Thread.sleep(1000)

  if (chatNode) {
    // Only log errors in chat mode.
    Logger.logLevels = Nil
    handler.addMessageObserver(new ChatLogger(transmissionObserver))
  }

  if (startingNode) {
    // Start first round as first node
    transmissionObserver.startMessageRound()
  }

  if (experimentNode) {
    val experimentObserver = new ExperimentObserver(handler, transmissionObserver)
    handler.addMessageObserver(experimentObserver)
  }

  // If we are a client node.
  if (!startingNode) {
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
