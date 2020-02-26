package nl.tudelft

import akka.actor.typed.ActorSystem
import nl.tudelft.GreeterMain.SayHello

object Main extends App {
  val greeterMain: ActorSystem[GreeterMain.SayHello] = ActorSystem(GreeterMain(), "AkkaQuickStart")
  greeterMain ! SayHello("Charles")
}