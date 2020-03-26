package nl.tudelft.fruitarian.models

import nl.tudelft.fruitarian.p2p.Address

import scala.util.Random

case class Peer(address: Address, seed: Int) {
	val r = new Random(seed)
	var roundId = 0

	def getRandomBytesForRound(size: Int, round: Int): List[Byte] = {
		while (roundId < round) {
			getRandomByteArray(size)
			roundId += 1
		}
		val bytes = getRandomByteArray(size)
		roundId += 1
		bytes
	}

	// Get a list of random bytes with a specific size.
	private def getRandomByteArray(size: Int): List[Byte] = {
			r.nextBytes(size).toList
	}
}
