package com.bluematador

import cats.effect._
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import scala.util.{Failure, Success, Try}

object RelayServer extends IOApp {

  implicit val logger: Logger[IO] =
    Slf4jLogger.getLogger[IO]

  override def run(args: List[String]): IO[ExitCode] =
    args
      .headOption
      .fold(IO pure System.err.println(s"Usage: sbt 'run {port number}' ") as ExitCode.Error)(value =>
        Try { value.toInt } match {
          case Failure(exception) => IO pure System.err.println(s"$exception") as ExitCode.Error
          case Success(port) => Relay.boostrap[IO](port).as(ExitCode.Success)
        }
      )

}
