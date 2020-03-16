package nl.tudelft.fruitarian.p2p.tcp

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorSystem, Props}
import akka.io.Tcp._
import akka.io.{IO, Tcp}
import nl.tudelft.fruitarian.p2p.Msg

object Server {
  def props(host: InetSocketAddress, callback: Msg => Unit) =
    Props(classOf[Server], host, callback);
}

/**
 * Listens to TCP connections from the outside.
 * Upon another client connecting to the server a tcp.ConnectionHandler actor
 * is set up to deal with the connection.
 */
class Server(host: InetSocketAddress, callback: Msg => Unit) extends
  Actor {
  implicit val system: ActorSystem = context.system

  // Bind the server to the given host.
  IO(Tcp) ! Bind(self, host)

  /**
   * Upon receiving a TCP message.
   */
  def receive: Receive = {
    // When the bound to our IO(Tcp) listener is completed.
    case b @ Bound(localAddress) =>
      println(s"[S] Listening on [$localAddress]")

    // When the bound to our IO(Tcp) listener failed.
    case CommandFailed(_: Bind) => context.stop(self)

    // Upon connection to the socket, set up an actor to handle that specific
    // connection.
    case c @ Connected(remote, local) =>
      val handler = context.actorOf(ConnectionHandler.props(callback))
      val connection = sender()
      connection ! Register(handler)
      println(s"[S] Client connected from [$remote]")
  }
}
