package nl.tudelft.fruitarian

import akka.actor.Actor
import akka.io.Tcp.Connected
import akka.util.ByteString
import nl.tudelft.fruitarian.tcp.Client.{ClientReceived, ClientSend}


class Logger extends Actor {

  def receive: Receive = {
    case Connected(remote, local) =>
      println("Connected to " + remote)
      println("Connect from " + local)
    case ClientReceived(data) =>
      println("Client Received: " + data.utf8String)
    case x: String => println(x)
  }
}
