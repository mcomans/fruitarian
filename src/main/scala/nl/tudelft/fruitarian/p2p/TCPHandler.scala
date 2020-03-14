package nl.tudelft.fruitarian.p2p

import java.net.InetSocketAddress

import akka.actor.{ActorRef, ActorSystem}
import nl.tudelft.fruitarian.p2p.tcp.Client.ClientSend

case class Address(socket: InetSocketAddress)
case class TCPConnection(address: Address, actor: ActorRef)

class TCPHandler() {
  private val sys = ActorSystem("TCP")
  val serverMessageBus = ServerMessageBus
  private val serverActor = sys.actorOf(
    tcp.Server.props(serverMessageBus.onIncomingMessage), "TCPServer")
  private var connections: List[TCPConnection] = Nil


  def sendMessage(to: Address, msg: ByteMsg): Unit = {
    val connection: TCPConnection = connections.find(_.address == to)
      .getOrElse(setupConnectionTo(to))
    connection.actor ! ClientSend(msg)
  }

  def setupConnectionTo(to: Address): TCPConnection = {
    val connection = TCPConnection(
      to,
      sys.actorOf(tcp.Client.props(to.socket, serverMessageBus
        .onIncomingMessage), "TCPClient" + to.socket.getPort)
    )
    connections = connection :: connections
    connection
  }

  def shutdown(): Unit = {
    sys.terminate()
  }
}
