package nl.tudelft.fruitarian.p2p.tcp

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorSystem, Props}
import akka.io.Tcp._
import akka.io.{IO, Tcp}
import akka.util.ByteString
import nl.tudelft.fruitarian.p2p.ByteMsg
import nl.tudelft.fruitarian.p2p.tcp.Client.ClientSend

object Client {
  final case class ClientSend(msg: ByteMsg)
  final case class ClientReceived(msg: ByteMsg)

  def props(remote: InetSocketAddress, callback: (ByteMsg) => Unit) =
    Props(classOf[Client], remote, callback);
}

/**
 * The TCP client sets up a TCP connection to a given remote. Keeping a
 * listener actor up to date on what happens on that TCP connection.
 * @param remote The remote address to connect to.
 */
class Client(remote: InetSocketAddress, callback: ByteMsg => Unit) extends
  Actor {
  implicit val system: ActorSystem = context.system

  // TODO: Implement message queue.

  // Connect to the desired tcp remote.
  IO(Tcp) ! Connect(remote)

  def receive = {
    // Upon connection failure, let listener know and kill.
    case CommandFailed(_: Connect) =>
      callback(ByteMsg("connection failed".getBytes()))
      context.stop(self)

    // Upon connection success.
    case c @ Connected(remote, local) =>
//      listener ! c
      val connection = sender()
      connection ! Register(self)
      context.become {
        // Upon getting binary data, send it through the connection.
        case ClientSend(data) =>
          connection ! Write(ByteString(data.bytes))

        // If the write failed due to OS buffer being full.
        case CommandFailed(w: Write) => callback(ByteMsg("Write Failed".getBytes()))

        // When data received, send it to the listener.
        case Received(data) => callback(ByteMsg(data.toArray))

        // On close command.
        case "close" => connection ! Close

        // Upon receiving the close message.
        case _: ConnectionClosed =>
          callback(ByteMsg("connection closed".getBytes()))
          context.stop(self)
      }
  }
}
