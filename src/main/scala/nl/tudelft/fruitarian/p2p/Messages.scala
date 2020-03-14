package nl.tudelft.fruitarian.p2p

case class ByteMsg(bytes: Array[Byte])
//case class StringMsg(msg: String) extends ByteMsg(bytes=msg.getBytes("utf-8"))
