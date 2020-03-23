package nl.tudelft.fruitarian.observers

import nl.tudelft.fruitarian.models.{DCnet, NetworkInfo, Peer}
import nl.tudelft.fruitarian.p2p.TCPHandler
import nl.tudelft.fruitarian.p2p.messages._
import nl.tudelft.fruitarian.patterns.Observer

import scala.collection.mutable.ListBuffer
import scala.util.Random

class EntryObserver(handler: TCPHandler, networkInfo: NetworkInfo) extends Observer[FruitarianMessage] {
  val r = new Random()
	var responses = new ListBuffer[List[Byte]]

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
    case AnnounceMessage(from, to, seed) =>
	    networkInfo.cliquePeers += Peer(from, Integer.parseInt(seed))

	    //Test dc net
	    printRandomNumbersPeers()
		  if (networkInfo.center) {
			  networkInfo.cliquePeers.foreach(p => {
				  handler.sendMessage(TransmitRequest(to, p.address))
			  })
		  }
    case TransmitRequest(from, to) =>
		  handler.sendMessage(TransmitMessage(to, from, DCnet.getRandom(networkInfo.cliquePeers.toList, 1)))
    case TransmitMessage(from, to, message) =>
		  responses = responses += message

	    println(responses)
	    if (responses.length == 2) {
		    var encryptedMessage = DCnet.encryptMessage("4", networkInfo.cliquePeers.toList)
		    var decryptedMessage = DCnet.decryptMessage((responses += encryptedMessage).toList)
		    println("RESPONSE: " + decryptedMessage)
	    }
  }

	// Get random seed.
  def getSeed: Int = {
	  r.nextInt()
  }

	// The function below prints the same random number for each pair of nodes.
	// TODO: remove this later
	def printRandomNumbersPeers(): Unit = {
		println("===============")
		networkInfo.cliquePeers.foreach(p => {
			println(p)
			println(p.getRandomNumber)
		})
		println("===============")
	}
}
