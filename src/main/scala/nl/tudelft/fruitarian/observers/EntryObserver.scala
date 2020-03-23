package nl.tudelft.fruitarian.observers

import nl.tudelft.fruitarian.models.{NetworkInfo, Peer, DCnet}
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
	    printRandomNumbersPeers()
    case EntryResponse(from, to, entryInfo) =>
	    entryInfo._2.foreach(p => {
		    val seed = getSeed
		    networkInfo.cliquePeers += Peer(p, seed)
		    handler.sendMessage(AnnounceMessage(to, p, seed.toString))
	    })
	    networkInfo.cliquePeers += Peer(from, Integer.parseInt(entryInfo._1))
	    printRandomNumbersPeers()
    case AnnounceMessage(from, _, seed) =>
	    networkInfo.cliquePeers += Peer(from, Integer.parseInt(seed))
	    printRandomNumbersPeers()
  }

	// Get random seed.
  def getSeed: Int = {
	  r.nextInt()
  }

	// The function below prints the same random number for each pair of nodes.
	// TODO: remove this later
	def printRandomNumbersPeers(): Unit = {
		println("===============")
		var bytes = DCnet.encryptMessage("hoi", networkInfo.cliquePeers.toList)
		println(new String(bytes))
		networkInfo.cliquePeers.foreach(p => {
			println(p)
			println(p.getRandomNumber)
		})
		println("===============")
	}
}
