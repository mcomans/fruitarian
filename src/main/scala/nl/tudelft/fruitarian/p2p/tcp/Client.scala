package nl.tudelft.fruitarian.p2p.tcp

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorSystem, Props}
import akka.io.Tcp._
import akka.io.{IO, Tcp}
import akka.util.ByteString
import nl.tudelft.fruitarian.p2p.{Address, MessageSerializer, Msg, SendMsg}

object Client {
  def props(remote: InetSocketAddress, callback: Msg => Unit) =
    Props(classOf[Client], remote, callback);
}

/**
 * The TCP client sets up a TCP connection to a given remote. Keeping a
 * listener actor up to date on what happens on that TCP connection.
 * @param remote The remote address to connect to.
 */
class Client(remote: InetSocketAddress, callback: Msg => Unit) extends
  Actor {
  implicit val system: ActorSystem = context.system
  var queue: List[Msg] = Nil

  // Connect to the desired tcp remote.
  IO(Tcp) ! Connect(remote)

  def receive = {
    // Upon connection failure, let listener know and kill.
    case CommandFailed(_: Connect) =>
      println(s"[C] Failed establishing new connection to [$remote]")
      context.stop(self)

    // When a message is sent but the connection is not yet ready, enqueue it.
    case SendMsg(msg: Msg) => queue = msg :: queue

    // Upon connection success.
    case c @ Connected(remote, local) =>
      val connection = sender()
      connection ! Register(self)

      // Clear message queue when connection was established.
      queue.foreach((msg: Msg) => connection ! sendMsg(msg))
      queue = Nil

      println(s"[C] Connection established to [$remote]")

      context.become {
        // Upon getting binary data, send it through the connection.
        case SendMsg(msg: Msg) =>
          connection ! sendMsg(msg)

        // If the write failed due to OS buffer being full.
        case CommandFailed(w: Write) => println("[C] Write Failed")

        // When data received, send it to the listener.
        case Received(data: ByteString) =>
          val msg: Msg = MessageSerializer.deserialize(data.utf8String)
          // Set the msg header from field to the actual receiver value.
          msg.header.from = Address(remote)
          callback(msg)

        // On close command.
        case "close" => connection ! Close

        // Upon receiving the close message.
        case _: ConnectionClosed =>
          println(s"[C] Connection Closed with [$remote]")
          context.stop(self)
      }
  }

  def sendMsg(msg: Msg) = Write(ByteString(MessageSerializer.serializeMsg(msg)))
}
