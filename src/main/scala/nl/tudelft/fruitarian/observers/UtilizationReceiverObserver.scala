package nl.tudelft.fruitarian.observers

import nl.tudelft.fruitarian.p2p.messages.{FruitarianMessage, ResultMessage}
import nl.tudelft.fruitarian.p2p.{MessageSerializer, TCPHandler}
import nl.tudelft.fruitarian.patterns.Observer

class UtilizationReceiverObserver(handler: TCPHandler, transmissionObserver: TransmissionObserver) extends
	Observer[FruitarianMessage] {
	var totalBytesReceived = 0
	var messageBytesReceived = 0

	override def receiveUpdate(event: FruitarianMessage): Unit = event match {
		case ResultMessage(from, to, message) if message != "TIMEOUT" =>
			totalBytesReceived += MessageSerializer.serializeMsg(event).getBytes().length
			messageBytesReceived += message.getBytes().length
			println("[UTILIZATION_RECEIVER] total: " + totalBytesReceived
				+ " message: " + messageBytesReceived
				+ " avg: " + messageBytesReceived.toFloat / totalBytesReceived)
		case _ =>
			totalBytesReceived += MessageSerializer.serializeMsg(event).getBytes().length
	}
}