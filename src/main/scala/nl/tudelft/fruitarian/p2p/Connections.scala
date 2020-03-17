package nl.tudelft.fruitarian.p2p

/** Singleton for connections management. */
object Connections {
  private var connections: List[TCPConnection] = Nil

  def addConnection(connection: TCPConnection): Unit =
    connections = connection :: connections

  def findConnectionFor(address: Address): Option[TCPConnection] =
    connections.find(_.address == address)

  def closeConnections(): Unit = connections.foreach(_.actor ! "close")
}
