package nl.tudelft.fruitarian.p2p

import nl.tudelft.fruitarian.patterns.Subject

object ServerMessageBus extends Subject[Msg] {
  def onIncomingMessage(message: Msg): Unit = {
    super.notifyObservers(message)
  }
}
