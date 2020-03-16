package nl.tudelft.fruitarian.p2p.tcp

import java.net.{InetSocketAddress}

import akka.actor.{Actor, Props}
import nl.tudelft.fruitarian.p2p.{Address, MessageSerializer, Msg}

object ConnectionHandler {
  def props(remote: InetSocketAddress, callback: Msg => Unit) =
    Props(classOf[ConnectionHandler], remote, callback);
}

class ConnectionHandler(remote: InetSocketAddress, callback: Msg => Unit) extends Actor {
  import akka.io.Tcp._

  def receive: Receive = {
    // When the TCP connection is closed, kill this node.
    case PeerClosed => {
      println("[S] Client connection closed")
      context.stop(self)
    }

    // Send any other event to the listener.
    case Received(data) =>
      val msg = MessageSerializer.deserialize(data.utf8String)
      msg.header.from = Address(remote)
      callback(msg)
  }
}
