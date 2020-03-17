package nl.tudelft.fruitarian.p2p

import java.net.InetSocketAddress

import akka.actor.ActorRef


case class Address(socket: InetSocketAddress)
case class TCPConnection(address: Address, actor: ActorRef)

/* Enum value for the types of messages that can be sent. */
object MsgType extends Enumeration {
  val Bytes = Value("BYTES")
  val Text = Value("TEXT")
}

/** The Header of a message. */
case class MsgHeader(msgType: MsgType.Value, var from: Address,
                     to: Address)

/** An abstract structure that defines the general structure of a message.
 * header: The header of the message, containing metadata.
 * body: The body of the message, containing the data to be sent.
 */
case class Msg(header: MsgHeader, body: Array[Byte])

/** Case class used to define an actor command to given send message. */
final case class SendMsg(msg: Msg)
