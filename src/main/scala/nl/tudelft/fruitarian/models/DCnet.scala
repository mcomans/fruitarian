package nl.tudelft.fruitarian.models

import scala.collection.mutable.{ArrayBuffer, ListBuffer}
import scala.util.Random

object DCnet {
	val MESSAGE_SIZE = 280

	var transmitRequestsSent = 0
	var responses = new ListBuffer[List[Byte]]

	// Get random seed.
	def getSeed: Int = {
		new Random().nextInt()
	}

	// For the node that needs to transmit a random message.
	// It calculates the xor value of the random values of all peers.
	def getRandom(peers: List[Peer]): List[Byte] = {
		var res = Array.fill[Byte](MESSAGE_SIZE)(0)
		peers.foreach(p => {
			// Get random bytes depending on the size of the message.
			val randomBytes = p.getRandomByteArray(MESSAGE_SIZE)
			// Calculate xor value.
			res = res.zipWithIndex.map(b => {
				val bytes = randomBytes(b._2)
				(b._1 ^ bytes).toByte
			})
		})
		res.toList
	}

	// For the node that wants to transmit a message.
	// Encrypt message using the random values from the peers
	// based on the the DC-net method.
	def encryptMessage(message: String, peers: List[Peer]): List[Byte] = {
		// Get xor value of the random values of all peers.
		val rand = getRandom(peers)
		var res = new ArrayBuffer[Byte]
		// Convert each char of the message to a dc-net message.
		formatMessageSize(message).zipWithIndex.foreach(c => {
			val binaryChar = getBinaryFormat(c._1.toByte)
			val binaryRand = getBinaryFormat(rand(c._2))
			res += convertToDCMessage(binaryChar, binaryRand)
		})
		res.toList
	}

	/**
	 * @return whether we have enough messages to decrypt.
	 */
	def canDecrypt: Boolean = {
		transmitRequestsSent > 0 && transmitRequestsSent == responses.length
	}

	/**
	 * Decrypt the received messages.
	 * Empties the received messages array after decryption.
	 * @return The decrypted message.
	 * @throws Exception when the amount of transmission requests sent does not
	 *                   match the amount of messages received.
	 */
	def decryptReceivedMessages(): String = {
		if (!canDecrypt) {
			throw new Exception("The amount of TransmitRequests sent does not " +
				"match the amount of responses.")
		}
		val msg = decryptMessage(responses.toList)
		responses = new ListBuffer[List[Byte]]
		transmitRequestsSent = 0
		msg
	}

	// For the center node that wants to reveal the original message.
	// Decrypts list of bytes by taking the xor value and converting it to a
	// string value: the original message.
	def decryptMessage(values: List[List[Byte]]): String = {
		var res = Array.fill[Byte](MESSAGE_SIZE)(0)
		// Loop over the byte lists from the other nodes.
		values.foreach(l => {
			// Calculate xor value.
			res = res.zipWithIndex.map(b => {
				val bytes = l(b._2)
				(b._1 ^ bytes).toByte
			})
		})
		// The original message.
		new String(res).replaceAll("""(?m)\s+$""","")
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

	// Checks the message size and throws an error when it is too long.
	def formatMessageSize(message: String): String = message.length compare MESSAGE_SIZE match {
		case 0 => message
		case 1 => throw new Exception("Message size exceeded. Maximum message size is "
			+ MESSAGE_SIZE + " characters.")
		case -1 => message.concat(List.fill(MESSAGE_SIZE - message.length)(' ').mkString)
	}
}
