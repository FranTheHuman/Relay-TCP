package com.bluematador

import cats.effect._
import com.bluematador.models.SocketAddressData
import com.comcast.ip4s.{Hostname, IpLiteralSyntax, Port}
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

object EchoServer extends IOApp {

  implicit val logger: Logger[IO] =
    Slf4jLogger.getLogger[IO]

  override def run(args: List[String]): IO[ExitCode] =
    args match {
      case ::(head, next) if next.nonEmpty =>
        Echo.boostrap[IO](
          SocketAddressData(
            Hostname.fromString(head).getOrElse(host"localhost"),
            next.headOption.flatMap(p => Port.fromInt(p.toInt)).getOrElse(port"8080")
          )
        ).map(_ => IO.never).as(ExitCode.Success)
      case _ => IO pure System.err.println(s"EchoServer needs 2 params (Host & Port)") as ExitCode.Error
    }

}
