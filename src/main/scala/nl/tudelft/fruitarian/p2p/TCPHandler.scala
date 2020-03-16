package nl.tudelft.fruitarian.p2p

import java.net.InetSocketAddress

import akka.actor.{ActorRef, ActorSystem}
import nl.tudelft.fruitarian.patterns.Observer



class TCPHandler() {
  val serverHost = new InetSocketAddress("0.0.0.0", 5000)

  private val serverMessageBus = ServerMessageBus
  private val sys = ActorSystem("TCP")
  private val serverActor = sys.actorOf(tcp.Server.props(
      serverHost, serverMessageBus.onIncomingMessage),
    "TCPServer")

  /* Store a list of active connections to other nodes for further reference */
  // TODO: Filter nodes that have been killed.
  private var connections: List[TCPConnection] = Nil


  def sendMessage(msg: Msg): Unit = {
    val connection: TCPConnection = connections.find(_.address == msg.header.to)
      .getOrElse(setupConnectionTo(msg.header.to))
    connection.actor ! SendMsg(msg)
  }

  def addMessageObserver(observer: Observer[Msg]): Unit = {
    serverMessageBus.addObserver(observer)
  }

  def setupConnectionTo(to: Address): TCPConnection = {
    val connection = TCPConnection(
      to,
      sys.actorOf(tcp.Client.props(to.socket, serverMessageBus
        .onIncomingMessage))
    )
    connections = connection :: connections
    connection
  }

  def shutdown(): Unit = {
    sys.terminate()
  }
}
