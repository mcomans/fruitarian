package nl.tudelft.fruitarian.observers

import nl.tudelft.fruitarian.models.{DCnet, NetworkInfo, Peer}
import nl.tudelft.fruitarian.p2p.TCPHandler
import nl.tudelft.fruitarian.p2p.messages._
import nl.tudelft.fruitarian.patterns.Observer

import scala.util.Random

class EntryObserver(handler: TCPHandler, networkInfo: NetworkInfo) extends Observer[FruitarianMessage] {
  override def receiveUpdate(event: FruitarianMessage): Unit = event match {
    case EntryRequest(from, to) =>
			// TODO: Find a better way to set our own address. This could allow
			//  malicious entries as we rely on the sender to set the correct header
			//  field.
			networkInfo.ownAddress = to
	    // Generate and send common seed to entry node.
      val seed = DCnet.getSeed
	    handler.sendMessage(EntryResponse(to, from, (seed.toString, networkInfo.getPeers)))
	    networkInfo.cliquePeers += Peer(from, seed)
    case EntryResponse(from, to, entryInfo) =>
	    // Generate and send seeds to all peers.
	    entryInfo._2.foreach(p => {
		    val seed = DCnet.getSeed
		    networkInfo.cliquePeers += Peer(p, seed)
		    handler.sendMessage(AnnounceMessage(to, p, seed.toString))
	    })
	    // Add peer with common seed value.
	    networkInfo.cliquePeers += Peer(from, Integer.parseInt(entryInfo._1))
    case AnnounceMessage(from, to, seed) =>
	    networkInfo.cliquePeers += Peer(from, Integer.parseInt(seed))

		case _ =>
  }
}
