package nl.tudelft.fruitarian.models

import nl.tudelft.fruitarian.p2p.Address

import scala.util.Random

case class Peer(address: Address, seed: Int) {
	val r = new Random(seed)

	// Get a list of random bytes with a specific size.
	def getRandomByteArray(size: Int): List[Byte] = {
			r.nextBytes(size).toList
	}
}

