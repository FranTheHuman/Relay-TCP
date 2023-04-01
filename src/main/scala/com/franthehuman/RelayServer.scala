package com.franthehuman

import cats.effect.{ExitCode, IO, IOApp}
import com.franthehuman.PortsPool.PortsPoolImpl
import com.franthehuman.Relay.RelayTcp

import scala.util.{Failure, Success, Try}

object RelayServer extends IOApp {

  override def run(args: List[String]): IO[ExitCode] =
    args
      .headOption
      .fold(IO pure System.err.println(s"Usage: sbt 'run {port number}' ") as ExitCode.Error)(value =>
        Try {
          value.toInt
        } match {
          case Failure(_) => IO pure System.err.println(s"Port argument must be a number!") as ExitCode.Error
          case Success(port) => new RelayTcp[IO] relay new PortsPoolImpl(port) as ExitCode.Success
        }
      )

}
