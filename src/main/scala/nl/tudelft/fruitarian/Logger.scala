package nl.tudelft.fruitarian



object Logger {
  object Level extends Enumeration {
    type level = Value
    val INFO, DEBUG, ERROR = Value
  }
  import Level._

  var logLevels: Seq[Level.Value] = List(INFO, DEBUG, ERROR)

  def log(msg: String, lvl: Level.Value): Unit = {
    if (logLevels.contains(lvl)){
      println(s"[$lvl] $msg")
    }
  }
}
