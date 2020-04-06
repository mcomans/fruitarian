package nl.tudelft.fruitarian.observers


import java.time.LocalDate
import java.util.concurrent.Executors

import nl.tudelft.fruitarian.p2p.messages.{AnnounceMessage, EntryRequest, FruitarianMessage, ResultMessage}
import nl.tudelft.fruitarian.patterns.Observer

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}
import scala.io.StdIn.readLine


/**
 * The Chat logger is used for when the application is in chat mode.
 * This mode is implemented for demonstration purposes.
 */
class ChatLogger(transmissionObserver: TransmissionObserver) extends Observer[FruitarianMessage] {
  protected implicit val context: ExecutionContextExecutor = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(2))
  case class ChatMessage(datetime: LocalDate, msg: String)
  def stripNonReadableBytes(msg: String): String = BasicLogger.stripNonReadableBytes(msg)

  val MESSAGE_HISTORY = 5
  var msgHistory: List[ChatMessage] = Nil
  var inputFuture: Future[String] = _

  private def renderMessage(msg: ChatMessage): Unit =
    println(msg.msg)
  private def requestMessage(): Future[String] = Future {
    readLine(" > ")
  }

  /**
   * Render a simple menu that allows
   */
  def renderMenu(): Unit = {
    inputFuture = requestMessage()
    inputFuture.onComplete(msg => {
      if (msg.isSuccess) {
        msg.get match {
          case "/debug" => println(transmissionObserver.roundId)
          case "/inbox" => renderInbox()
          case "/inbox clear" => clearInbox()
          case "/help" => renderHelp()
          case message if message.startsWith("/send ") =>
            val actualMessage = message.replace("/send ", "")
            transmissionObserver.queueMessage(actualMessage)
          case _ => renderHelp()
        }
      }
      renderMenu()
    })
  }

  def renderInbox(): Unit = {
    if (msgHistory.isEmpty) {
      println("Nothing here.")
    } else {
      println(s"Found ${msgHistory.length} message(s)")
      msgHistory.foreach(msg => renderMessage(msg))
    }
  }

  def renderHelp(): Unit = {
    println("Help Page for Fruitarian")
    println("  /inbox\t\t\tShows your inbox")
    println("  /inbox clear\t\tClears your inbox")
    println("  /help\t\t\tShows this page")
    println("  /send <msg>\t\tSends the <msg> to the clique")
  }

  def clearInbox(): Unit = {
    msgHistory = Nil
    println("Inbox cleared!")
  }


  // Initial render with no message received.
  renderMenu()

  override def receiveUpdate(event: FruitarianMessage): Unit = event match {
    case EntryRequest(_, _, _) | AnnounceMessage(_, _, _) =>
      val msg = ChatMessage(LocalDate.now(), s"<Clique>: Node joined!")
      msgHistory = msgHistory :+ msg
    case ResultMessage(_, _, message) => stripNonReadableBytes(message) match {
      /* In case we get a non-empty message print it. */
      case "TIMEOUT" =>
      case s if !s.isEmpty =>
        val msg = ChatMessage(LocalDate.now(), s"<Clique>: $s")
        msgHistory = msgHistory :+ msg
      case _ =>
    }
    case _ =>
  }
}
