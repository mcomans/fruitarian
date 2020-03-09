package nl.tudelft.fruitarian.tcp

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.io.{IO, Tcp}
import akka.util.ByteString
import java.net.InetSocketAddress

import akka.io.Tcp.{Close, CommandFailed, Connect, Connected, ConnectionClosed, Received, Register, Write}
import nl.tudelft.fruitarian.tcp.Client.{ClientReceived, ClientSend}

object Client {
  final case class ClientSend(data: ByteString)
  final case class ClientReceived(data: ByteString)

  def props(remote: InetSocketAddress, replies: ActorRef) =
    Props(classOf[Client], remote, replies);
}

/**
 * The TCP client sets up a TCP connection to a given remote. Keeping a
 * listener actor up to date on what happens on that TCP connection.
 * @param remote The remote address to connect to.
 * @param listener The listener actor that receives updates about the TCP
 *                 connection.
 */
class Client(remote: InetSocketAddress, listener: ActorRef) extends Actor {
  implicit val system: ActorSystem = context.system

  // Connect to the desired tcp remote.
  IO(Tcp) ! Connect(remote)

  def receive = {
    // Upon connection failure, let listener know and kill.
    case CommandFailed(_: Connect) =>
      listener ! "connection failed"
      context.stop(self)

    // Upon connection success.
    case c @ Connected(remote, local) =>
      listener ! c
      val connection = sender()
      connection ! Register(self)
      context.become {
        // Upon getting binary data, send it through the connection.
        case ClientSend(data) =>
          connection ! Write(data)

        // If the write failed due to OS buffer being full.
        case CommandFailed(w: Write) =>
          listener ! "write failed"

        // When data received, send it to the listener.
        case Received(data) => listener ! ClientReceived(data)

        // On close command.
        case "close" => connection ! Close

        // Upon receiving the close message.
        case _: ConnectionClosed =>
          listener ! "connection closed"
          context.stop(self)
      }
  }
}
