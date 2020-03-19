package nl.tudelft.fruitarian.models

import nl.tudelft.fruitarian.p2p.Address

import scala.collection.mutable.{ArrayBuffer, ListBuffer}

class NetworkInfo {
  val cliquePeers: ArrayBuffer[Peer] = ArrayBuffer[Peer]()

  def getPeers: List[Address] = {
    val r = new ListBuffer[Address]
    cliquePeers.foreach(p => r += p.address)
    r.toList
  }
}