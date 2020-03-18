package nl.tudelft.fruitarian.observers

import nl.tudelft.fruitarian.models.{NetworkInfo, Peer}
import nl.tudelft.fruitarian.p2p.TCPHandler
import nl.tudelft.fruitarian.p2p.messages.{AnnounceMessage, EntryRequest, EntryResponse, FruitarianMessage}
import nl.tudelft.fruitarian.patterns.Observer

class EntryObserver(handler: TCPHandler, networkInfo: NetworkInfo) extends Observer[FruitarianMessage] {
  override def receiveUpdate(event: FruitarianMessage): Unit = event match {
    case EntryRequest(from, to) =>
      handler.sendMessage(EntryResponse(to, from, networkInfo.cliquePeers.toList))
      networkInfo.cliquePeers += Peer(from)
    case EntryResponse(from, to, peerList) =>
      networkInfo.cliquePeers ++= peerList
      networkInfo.cliquePeers.foreach(p => handler.sendMessage(AnnounceMessage(to, p.address)))
      networkInfo.cliquePeers += Peer(from)
    case AnnounceMessage(from, _) =>
      networkInfo.cliquePeers += Peer(from)
  }
}
