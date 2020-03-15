package nl.tudelft.fruitarian

import java.net.InetSocketAddress

import akka.actor.ActorSystem
import nl.tudelft.fruitarian.p2p.{Address, Msg, MsgHeader, MsgType,
  TCPHandler}
import nl.tudelft.fruitarian.patterns.Observer

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

object Main extends App {
  // Only create system for the scheduleOnce methods.
  val sys = ActorSystem("system")

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

  sys.scheduler.scheduleOnce(1.seconds) {
    // Message will fail due to connection not being set up. Should get fixed
    // on queue implementation.
    handler.sendMessage(helloWorldMessage)
  }

  sys.scheduler.scheduleOnce(2.seconds) {
    // Message will succeed as connection should be up.
    handler.sendMessage(helloWorldMessage)
  }

  sys.scheduler.scheduleOnce(10.seconds) {
    println("System shutdown...")
    // Kill everyone
    handler.shutdown()
    sys.terminate()
  }
}
