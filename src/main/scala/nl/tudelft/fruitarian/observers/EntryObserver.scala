package nl.tudelft.fruitarian.observers

import nl.tudelft.fruitarian.models.{NetworkInfo, Peer}
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
	    printInfo()
    case EntryResponse(from, to, entryInfo) =>
	    entryInfo._2.foreach(p => {
		    val seed = getSeed
		    networkInfo.cliquePeers += Peer(p, seed)
		    handler.sendMessage(SeedMessage(to, p, seed.toString))
	    })
	    networkInfo.cliquePeers += Peer(from, Integer.parseInt(entryInfo._1))
	    printInfo()
    case SeedMessage(from, to, seed) =>
	    networkInfo.cliquePeers += Peer(from, Integer.parseInt(seed))
		  printInfo()
  }

  def getSeed: Int = {
	  r.nextInt()
  }

	def printInfo(): Unit = {
		println("===============")
		networkInfo.cliquePeers.foreach(p => {
			println(p)
			println(p.getCoinToss)
		})
		println("===============")
	}
}
