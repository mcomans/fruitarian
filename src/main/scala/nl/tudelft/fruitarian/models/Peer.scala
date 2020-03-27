package nl.tudelft.fruitarian.models

import nl.tudelft.fruitarian.p2p.Address

import scala.util.Random

case class Peer(address: Address, seed: Int) {
	val r = new Random(seed)
	var roundId = 0

	/**
	 * Get random bytes for a given round.
	 *
	 * This method uses the round number to make sure it is synchronised with
	 * the random number generator at the other clients for this peer, which are
	 * based on the same seed.
	 *
	 * This method is synchronised as it adjusts the roundId and uses the random
	 * generator. In a high load situation different threads could use this
	 * function and potentially unsynchronise the roundId and the amount of times
	 * the random number generator has been used. This causes the method to
	 * assume its giving the correct random bytes for a round while the actual
	 * produces bytes are ahead by a round.
	 *
	 * @param size The amount of random bytes required.
	 * @param round The round for which these random bytes are generated. This
	 *              makes sure the generated bytes are the same on both sides
	 *              of the connection with this peer.
	 * @return A random set of bytes based on the seed shared with this peer.
	 */
	def getRandomBytesForRound(size: Int, round: Int): List[Byte] = this.synchronized {
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
