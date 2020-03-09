package nl.tudelft.fruitarian

import java.net.InetSocketAddress

import akka.actor.AbstractActor.Receive
import akka.actor.{ActorSystem, Kill, PoisonPill, Props}
import akka.util.ByteString
import nl.tudelft.fruitarian.tcp.Client.ClientSend

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

object Main extends App {

  val sys = ActorSystem("system")
  val serverActor = sys.actorOf(Props[tcp.Server], "TCPServer")
  val loggerActor = sys.actorOf(Props[Logger], "Logger")
  val clientActor = sys.actorOf(tcp.Client.props(new InetSocketAddress("localhost", 5000), loggerActor), "TCPClient")


  def sendHeartbeat(): Unit = {
    clientActor ! ClientSend(ByteString("Heartbeat"))
  }
  sys.scheduler.scheduleWithFixedDelay(500.millis, 2.seconds, clientActor, ClientSend(ByteString("Heartbeat")))


  sys.scheduler.scheduleOnce(10.seconds) {
    println("System shutdown!")
    // Kill everyone
    sys.terminate()
  }
}
