package nl.tudelft.fruitarian.models

import nl.tudelft.fruitarian.p2p.Address

import scala.collection.mutable.{ArrayBuffer, ListBuffer}

class NetworkInfo(centerNode: Boolean) {
  val center: Boolean = centerNode
  val cliquePeers: ArrayBuffer[Peer] = ArrayBuffer[Peer]()
  var responses = new ListBuffer[List[Byte]]

  // Returns the addresses of all peers.
  def getPeers: List[Address] = {
    val r = new ListBuffer[Address]
    cliquePeers.foreach(p => r += p.address)
    r.toList
  }
}