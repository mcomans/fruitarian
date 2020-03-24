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
	    // Generate and send common seed to entry node.
      val seed = getSeed
	    handler.sendMessage(EntryResponse(to, from, (seed.toString, networkInfo.getPeers)))
	    networkInfo.cliquePeers += Peer(from, seed)
    case EntryResponse(from, to, entryInfo) =>
	    // Generate and send seeds to all peers.
	    entryInfo._2.foreach(p => {
		    val seed = getSeed
		    networkInfo.cliquePeers += Peer(p, seed)
		    handler.sendMessage(AnnounceMessage(to, p, seed.toString))
	    })
	    // Add peer with common seed value.
	    networkInfo.cliquePeers += Peer(from, Integer.parseInt(entryInfo._1))
    case AnnounceMessage(from, to, seed) =>
	    networkInfo.cliquePeers += Peer(from, Integer.parseInt(seed))

	    // Todo: replace this when message transfer is implemented.
	    // Center node sends request when 3 other peers have entered the network.
	    // The request asks all nodes to send their random xor values.
		  if (networkInfo.center && networkInfo.cliquePeers.length == 3) {
			  networkInfo.cliquePeers.foreach(p => {
				  handler.sendMessage(TransmitRequest(to, p.address))
			  })
		  }
    case TransmitRequest(from, to) =>
	    // Todo: replace this when message transfer is implemented.
	    // Send message back to the center node with random xor value.
	    // Message size should be known as well.
	    handler.sendMessage(TransmitMessage(to, from, DCnet.getRandom(networkInfo.cliquePeers.toList)))
    case TransmitMessage(from, to, message) =>
		  networkInfo.responses += message

	    // Todo: replace this when message transfer is implemented.
	    // When the center node has three responses, encrypt a message based on
	    // the peers and decrypt it using the responses.
	    if (networkInfo.responses.length == 3) {
			    var encryptedMessage = DCnet.encryptMessage("Hi there!", networkInfo.cliquePeers.toList)
			    var decryptedMessage = DCnet.decryptMessage((networkInfo.responses += encryptedMessage).toList)
			    println("RESPONSE: " + decryptedMessage)
	    }
  }

	// Get random seed.
  def getSeed: Int = {
	  r.nextInt()
  }
}
