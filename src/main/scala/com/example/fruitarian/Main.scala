package com.example.fruitarian

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._

object Main extends IOApp {
  def run(args: List[String]) =
    FruitarianServer.stream[IO].compile.drain.as(ExitCode.Success)
}