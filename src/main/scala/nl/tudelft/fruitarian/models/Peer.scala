package nl.tudelft.fruitarian.models

import nl.tudelft.fruitarian.p2p.Address

import scala.util.Random

case class Peer(address: Address, seed: Int) {
	val r = new Random(seed)

	def getRandomByteArray(size: Int): List[Byte] = {
			r.nextBytes(size).toList
	}

	def getRandomNumber: Int = {
		r.nextInt()
	}
}

