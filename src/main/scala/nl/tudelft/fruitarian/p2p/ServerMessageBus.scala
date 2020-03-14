package nl.tudelft.fruitarian.p2p

trait Observer[S] {
  def receiveUpdate(event: S);
}

trait Subject[S] {
  private var observers: List[Observer[S]] = Nil
  def addObserver(observer: Observer[S]) = observers = observer :: observers
  def notifyObservers(event: S) = observers.foreach(_.receiveUpdate(event))
}

object ServerMessageBus extends Subject[ByteMsg] {
  def onIncomingMessage(message: ByteMsg): Unit = {
    println("Incoming message: " + message.bytes.map(_.toChar).mkString)
    super.notifyObservers(message)
  }
}
