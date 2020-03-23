package nl.tudelft.fruitarian.models

import scala.collection.mutable.ArrayBuffer

object DCnet {
	val MESSAGE_SIZE = 1


	def encryptMessage(message: String, peers: List[Peer]): List[Byte] = {
		val rand = getRandom(peers, message.length)
		var index = 0
		var res = new ArrayBuffer[Byte]
		message.foreach(c => {
			val binaryChar = getBinaryFormat(c.toByte)
			val binaryRand = getBinaryFormat(rand(index))
			res += convertToDCMessage(binaryChar, binaryRand)
			index += 1
		})
		res.toList
	}

	def getBinaryFormat(input: Byte): String = {
		String.format("%8s", Integer.toBinaryString(input & 0xFF)).replace(' ', '0')
	}

	def convertToDCMessage(message: String, rand: String): Byte = {
		val res = (message zip rand).map {
			case ('0', b) => b
			case ('1', '1') => '0'
			case ('1', '0') => '1'
		}
		val x = Integer.parseInt(res.mkString(""), 2).toByte
		Integer.parseInt(res.mkString(""), 2).toByte
	}

	def decryptMessage(values: List[List[Byte]]): String = {
		var res = getEmptyArray(values(0).length)
		values.foreach(l => {
			var index = 0
			res = res.map(b => {
				val bytes = l(index)
				index += 1
				(b ^ bytes).toByte
			})
		})
		res.foreach(b => {
		})
		new String(res.toArray)
	}

	def getEmptyArray(size: Int): ArrayBuffer[Byte] = {
		var res = new ArrayBuffer[Byte]
		1 to size foreach { _ => res += 0 }
		res
	}

	def getRandom(peers: List[Peer], size: Int): List[Byte] = {
		var res = getEmptyArray(size)
		peers.foreach(p => {
			val randomBytes = p.getRandomByteArray(size)
			var index = 0
			res = res.map(b => {
				val bytes = randomBytes(index)
				index += 1
				(b ^ bytes).toByte
			})
		})
		res.toList
	}
}
