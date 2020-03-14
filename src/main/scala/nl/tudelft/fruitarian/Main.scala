package nl.tudelft.fruitarian

import java.net.InetSocketAddress

import akka.actor.ActorSystem
import nl.tudelft.fruitarian.p2p.{Address, ByteMsg, TCPHandler}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

object Main extends App {
  // Only create system for the scheduleOnce methods.
  val sys = ActorSystem("system")
  val handler = new TCPHandler()

  sys.scheduler.scheduleOnce(1.seconds) {
    // Message will fail due to connection not being set up. Should get fixed
    // on queue implementation.
    handler.sendMessage(
      Address(new InetSocketAddress("0.0.0.0", 5000)),
      ByteMsg("Connection".getBytes()))
  }

  sys.scheduler.scheduleOnce(2.seconds) {
    // Message will succeed as connection should be up.
    handler.sendMessage(
      Address(new InetSocketAddress("0.0.0.0", 5000)),
      ByteMsg("Test".getBytes()))
  }

  sys.scheduler.scheduleOnce(10.seconds) {
    println("System shutdown!")
    // Kill everyone
    handler.shutdown()
    sys.terminate()
  }
}
