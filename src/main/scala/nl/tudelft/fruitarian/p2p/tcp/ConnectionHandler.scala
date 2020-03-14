package nl.tudelft.fruitarian.p2p.tcp

import akka.actor.{Actor, Props}
import nl.tudelft.fruitarian.p2p.ByteMsg

object ConnectionHandler {
  def props(callback: (ByteMsg) => Unit) =
    Props(classOf[ConnectionHandler], callback);
}

class ConnectionHandler(callback: (ByteMsg) => Unit) extends Actor {
  import akka.io.Tcp._

  def receive: Receive = {
    // When the TCP connection is closed, kill this node.
    case PeerClosed => {
      callback(ByteMsg("Connection Closed".getBytes()))
      context.stop(self)
    }

    // Send any other event to the listener.
    case Received(data) => callback(ByteMsg(data.toArray))
  }
}
