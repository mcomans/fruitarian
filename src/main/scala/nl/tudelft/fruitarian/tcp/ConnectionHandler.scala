package nl.tudelft.fruitarian.tcp

import akka.actor.Actor

class ConnectionHandler extends Actor {
  import akka.io.Tcp._

  def receive: Receive = {
    // Upon receiving data, deal with it.
    case Received(data) =>
      println("Server received: " + data.utf8String)

    // When the TCP connection is closed, kill this node.
    case PeerClosed => context.stop(self)
  }
}
