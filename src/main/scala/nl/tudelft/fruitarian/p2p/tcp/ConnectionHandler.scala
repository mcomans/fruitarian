package nl.tudelft.fruitarian.p2p.tcp

import akka.actor.{Actor, Props}
import nl.tudelft.fruitarian.p2p.{MessageSerializer, Msg}

object ConnectionHandler {
  def props(callback: Msg => Unit) =
    Props(classOf[ConnectionHandler], callback);
}

class ConnectionHandler(callback: Msg => Unit) extends Actor {
  import akka.io.Tcp._

  def receive: Receive = {
    // When the TCP connection is closed, kill this node.
    case PeerClosed => {
      println("Client connection closed")
      context.stop(self)
    }

    // Send any other event to the listener.
    case Received(data) => callback(MessageSerializer.deserialize(data.utf8String))
  }
}
