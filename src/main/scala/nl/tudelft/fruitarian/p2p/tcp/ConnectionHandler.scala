package nl.tudelft.fruitarian.p2p.tcp

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorRef, Props}
import akka.util.ByteString
import nl.tudelft.fruitarian.p2p.{Address, MessageSerializer, Msg, SendMsg}

object ConnectionHandler {
  def props(connection: ActorRef, remote: InetSocketAddress, callback: Msg => Unit) =
    Props(classOf[ConnectionHandler], connection, remote, callback);
}

class ConnectionHandler(connection: ActorRef, remote: InetSocketAddress, callback: Msg => Unit) extends Actor {
  import akka.io.Tcp._

  // This receive function mostly follows the Client version.
  // TODO: Perhaps generalise Client such that it can be used for both server and client connections.
  def receive: Receive = {
    // Upon getting binary data, send it through the connection.
    case SendMsg(msg: Msg) =>
      connection ! Client.messageToWrite(msg)

    case "close" => connection ! Close

    // When the TCP connection is closed, kill this node.
    case PeerClosed =>
      println("[S] Client connection closed")
      context.stop(self)

    // Send any other event to the listener.
    case Received(data) =>
      val msg = MessageSerializer.deserialize(data.utf8String)
      msg.header.from = Address(remote)
      callback(msg)
  }
}
