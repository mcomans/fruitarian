package nl.tudelft.fruitarian.models

import scala.collection.mutable.{ArrayBuffer, ListBuffer}

object DCnet {
	val MESSAGE_SIZE = 1


		def encryptMessage(message: String, peers: List[Peer]): Array[Byte] = {
			val rand = getRandom(peers, message.length)
			var index = 0
			var res = new ArrayBuffer[Byte]
			message.foreach(c => {
				index = 0
				val binaryChar = c.toByte.toBinaryString
				val binaryRand = rand(index).toBinaryString
				res += convertToDCMessage(binaryChar, binaryRand)
			})
			res.toArray
		}

	def convertToDCMessage(message: String, rand: String): Byte = {
		val res = (message zip rand).map {
			case ('0', b) => b
			case ('1', '1') => '0'
			case ('1', '0') => '1'
		}
		Integer.parseInt(res.mkString(""), 2).toByte
	}

	//	def decryptMessage(values: List[Byte]): String = {
	//
	//	}

	def getEmptyList: ListBuffer[Byte] = {
		var res = new ListBuffer[Byte]
		1 to MESSAGE_SIZE foreach { _ => res += 0 }
		res
	}

	def getRandom(peers: List[Peer], size: Int): List[Byte] = {
		var res = getEmptyList
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
