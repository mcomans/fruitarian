package nl.tudelft.fruitarian.models

import scala.collection.mutable.ArrayBuffer

object DCnet {
	val MESSAGE_SIZE = 1

	// For the node that needs to transmit a random message.
	// It calculates the xor value of the random values of all peers.
	def getRandom(peers: List[Peer], size: Int): List[Byte] = {
		var res = getEmptyArray(size)
		peers.foreach(p => {
			// Get random bytes depending on the size of the message.
			val randomBytes = p.getRandomByteArray(size)
			var index = 0
			// Calculate xor value.
			res = res.map(b => {
				val bytes = randomBytes(index)
				index += 1
				(b ^ bytes).toByte
			})
		})
		res.toList
	}

	// For the node that wants to transmit a message.
	// Encrypt message using the random values from the peers
	// based on the the DC-net method.
	def encryptMessage(message: String, peers: List[Peer]): List[Byte] = {
		// Get xor value of the random values of all peers.
		val rand = getRandom(peers, message.length)
		var index = 0
		var res = new ArrayBuffer[Byte]
		// Convert each char of the message to a dc-net message.
		message.foreach(c => {
			val binaryChar = getBinaryFormat(c.toByte)
			val binaryRand = getBinaryFormat(rand(index))
			res += convertToDCMessage(binaryChar, binaryRand)
			index += 1
		})
		res.toList
	}

	// For the center node that wants to reveal the original message.
	// Decrypts list of bytes by taking the xor value and converting it to a
	// string value: the original message.
	def decryptMessage(values: List[List[Byte]]): String = {
		var res = getEmptyArray(values(0).length)
		// Loop over the byte lists from the other nodes.
		values.foreach(l => {
			var index = 0
			// Calculate xor value.
			res = res.map(b => {
				val bytes = l(index)
				index += 1
				(b ^ bytes).toByte
			})
		})
		res.foreach(b => {
		})
		// The original message.
		new String(res.toArray)
	}

	// Converts a byte into a binary string format.
	def getBinaryFormat(input: Byte): String = {
		String.format("%8s", Integer.toBinaryString(input & 0xFF))
			.replace(' ', '0')
	}

	// Converts an input binary string of a message char to an anonymized
	// char byte using the random binary string. This is the core DC-net method.
	def convertToDCMessage(input: String, rand: String): Byte = {
		val res = (input zip rand).map {
			case ('0', b) => b
			case ('1', '1') => '0'
			case ('1', '0') => '1'
		}
		Integer.parseInt(res.mkString(""), 2).toByte
	}

	// Get empty array with specific size.
	def getEmptyArray(size: Int): ArrayBuffer[Byte] = {
		var res = new ArrayBuffer[Byte]
		1 to size foreach { _ => res += 0 }
		res
	}
}
