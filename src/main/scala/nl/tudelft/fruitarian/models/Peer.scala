package nl.tudelft.fruitarian.models

import nl.tudelft.fruitarian.p2p.Address

import scala.util.Random

case class Peer(address: Address, seed: Int) {
	val r = new Random(seed)
	val MESSAGE_SIZE = 128

	def getRandomByteArray: Array[Byte] = {
			r.nextBytes(MESSAGE_SIZE)
	}

	def getRandomNumber: Int = {
		r.nextInt()
	}
}

