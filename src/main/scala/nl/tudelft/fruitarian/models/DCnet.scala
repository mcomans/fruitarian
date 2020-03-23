package nl.tudelft.fruitarian.models

import scala.collection.mutable.ArrayBuffer

object DCnet {
	val MESSAGE_SIZE = 1


	def encryptMessage(message: String, peers: List[Peer]): List[Byte] = {
		val rand = getRandom(peers, message.length)
		rand.foreach(b => {
			println("In encrypt: " + b.toBinaryString)
		})
		var index = 0
		var res = new ArrayBuffer[Byte]
		message.foreach(c => {
			index = 0
//			val binaryChar = c.toByte.toBinaryString
//			val binaryRand = rand(index).toBinaryString
			val binaryChar = getBinaryFormat(c.toByte)
			val binaryRand = getBinaryFormat(rand(index))
			println("Encrypt: " + convertToDCMessage(binaryChar, binaryRand).toBinaryString)
			res += convertToDCMessage(binaryChar, binaryRand)
		})
		res.toList
	}

	def getBinaryFormat(input: Byte): String = {
		String.format("%8s", Integer.toBinaryString(input & 0xFF)).replace(' ', '0')
	}

	def convertToDCMessage(message: String, rand: String): Byte = {
		println("MESSAGE: " + message)
		println("RAND: " + rand)
		val res = (message zip rand).map {
			case ('0', b) => b
			case ('1', '1') => '0'
			case ('1', '0') => '1'
		}
		println("CONVERTED:" + res.mkString(""))
		Integer.parseInt(res.mkString(""), 2).toByte
	}

	def decryptMessage(values: List[List[Byte]]): String = {
		var res = getEmptyArray(values(0).length)
		values.foreach(l => {
			var index = 0
			res = res.map(b => {
				val bytes = l(index)
				index += 1
				println("Decrypt: " + bytes.toBinaryString)
				(b ^ bytes).toByte
			})
		})
		res.foreach(b => {
			println("XOR RANDOM: " + b.toBinaryString)
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
				println("Random: " + p.address + " - " + bytes.toBinaryString)
				(b ^ bytes).toByte
			})
		})
		res.foreach(b => {
			println("XOR RANDOM: " + b.toBinaryString)
		})
		res.toList
	}
}
