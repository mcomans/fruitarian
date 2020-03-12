package nl.tudelft.fruitarian.tcp

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorSystem, Props}
import akka.io.Tcp._
import akka.io.{IO, Tcp}

/**
 * Listens to TCP connections from the outside.
 * Upon another client connecting to the server a tcp.ConnectionHandler actor
 * is set up to deal with the connection.
 */
class Server extends Actor {
  implicit val system: ActorSystem = context.system

  // Binding to localhost with port=0 means binding to localhost on a random port.
  IO(Tcp) ! Bind(self, new InetSocketAddress("0.0.0.0", 5000))
  /**
   * Upon receiving a TCP message.
   */
  def receive: Receive = {
    // When the bound to our IO(Tcp) listener is completed.
    case b @ Bound(localAddress) => context.parent ! b

    // When the bound to our IO(Tcp) listener failed.
    case CommandFailed(_: Bind) => context.stop(self)

    // Upon connection to the socket, set up an actor to handle that specific
    // connection.
    case c @ Connected(remote, local) =>
      val handler = context.actorOf(Props[ConnectionHandler])
      val connection = sender()
      connection ! Register(handler)
  }
}
