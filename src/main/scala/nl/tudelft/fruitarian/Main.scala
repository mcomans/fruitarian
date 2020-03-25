package nl.tudelft.fruitarian

import nl.tudelft.fruitarian.models.NetworkInfo
import nl.tudelft.fruitarian.observers.{BasicLogger, EntryObserver, Greeter, TransmissionObserver}
import nl.tudelft.fruitarian.p2p.{Address, TCPHandler}

object Main extends App {
  /* This example will start a Transmission Message Round with itself. */

  //TODO: remove when center node election is implemented.
  val networkInfo = if (args.length > 0) new NetworkInfo(false) else new NetworkInfo(true)

  val handler = if (args.length > 0) new TCPHandler(args(0).toInt) else new TCPHandler()
  networkInfo.ownAddress = Address(handler.serverHost)
  handler.addMessageObserver(BasicLogger)
  handler.addMessageObserver(new Greeter(handler))
  handler.addMessageObserver(new EntryObserver(handler, networkInfo))
  var transmissionObserver = new TransmissionObserver(handler, networkInfo)
  handler.addMessageObserver(transmissionObserver)

  Thread.sleep(1000)

  transmissionObserver.queueMessage("Hi there")

  if (args.length == 0) {
    // TODO: Actually start a message round once elected centre node.
    transmissionObserver.startMessageRound()
  }

  sys.addShutdownHook(() => {
    handler.shutdown()
  })
}
