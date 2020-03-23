package nl.tudelft.fruitarian.observers

import nl.tudelft.fruitarian.models.{DCnet, NetworkInfo, Peer}
import nl.tudelft.fruitarian.p2p.TCPHandler
import nl.tudelft.fruitarian.p2p.messages._
import nl.tudelft.fruitarian.patterns.Observer

import scala.util.Random

class EntryObserver(handler: TCPHandler, networkInfo: NetworkInfo) extends Observer[FruitarianMessage] {
  val r = new Random()

  override def receiveUpdate(event: FruitarianMessage): Unit = event match {
    case EntryRequest(from, to) =>
	    val seed = getSeed
	    println(networkInfo.getPeers)
	    handler.sendMessage(EntryResponse(to, from, (seed.toString, networkInfo.getPeers)))
	    networkInfo.cliquePeers += Peer(from, seed)
    case EntryResponse(from, to, entryInfo) =>
	    entryInfo._2.foreach(p => {
		    val seed = getSeed
		    networkInfo.cliquePeers += Peer(p, seed)
		    handler.sendMessage(AnnounceMessage(to, p, seed.toString))
	    })
	    networkInfo.cliquePeers += Peer(from, Integer.parseInt(entryInfo._1))
    case AnnounceMessage(from, to, seed) =>
	    networkInfo.cliquePeers += Peer(from, Integer.parseInt(seed))

	    println(networkInfo.cliquePeers)
		  if (networkInfo.center && networkInfo.cliquePeers.length == 3) {
			  networkInfo.cliquePeers.foreach(p => {
				  handler.sendMessage(TransmitRequest(to, p.address))
			  })
		  }
    case TransmitRequest(from, to) =>
	    handler.sendMessage(TransmitMessage(to, from, DCnet.getRandom(networkInfo.cliquePeers.toList, 9)))
    case TransmitMessage(from, to, message) =>
		  networkInfo.responses += message

	    if (networkInfo.responses.length == 3 && networkInfo.slot) {
		      networkInfo.slot = false
			    var encryptedMessage = DCnet.encryptMessage("Hoi Lullo", networkInfo.cliquePeers.toList)
			    var decryptedMessage = DCnet.decryptMessage((networkInfo.responses += encryptedMessage).toList)
			    println("RESPONSE: " + decryptedMessage)
	    }
  }

	// Get random seed.
  def getSeed: Int = {
	  r.nextInt()
  }
}
