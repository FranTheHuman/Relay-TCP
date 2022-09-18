package com.bluematador

import cats.effect._
import com.bluematador.Echo.EchoTcp
import com.bluematador.models.SocketAddressData
import com.comcast.ip4s.{Hostname, Port}

object EchoServer extends IOApp {

  override def run(args: List[String]): IO[ExitCode] =
    args match {
      case ::(head, next) if next.nonEmpty =>
        new EchoTcp[IO].echo(makeSocketAddressData(head, next)) >> IO(ExitCode.Success)

      case _ =>
        IO
          .pure(System.err.println(s"EchoServer needs 2 params (host & port)"))
          .as(ExitCode.Error)
    }

  val makeSocketAddressData: (String, List[String]) => SocketAddressData =
    (str1, str2) =>
      (for {
        host <- Hostname.fromString(str1)
        port <- str2.headOption.flatMap(p => Port.fromInt(p.toInt))
      } yield SocketAddressData(host, port)).getOrElse(SocketAddressData.empty)

}
