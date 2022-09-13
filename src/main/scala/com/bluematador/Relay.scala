package com.bluematador

import cats.effect.{Async, Concurrent}
import cats.effect.std.Console
import cats.implicits.{catsSyntaxTuple2Semigroupal, toFlatMapOps}
import com.comcast.ip4s.{IpLiteralSyntax, Port}
import fs2.io.net.Network
import fs2.text
import org.typelevel.log4cats.Logger

trait Relay {

  def boostrap[F[_] : Async : Concurrent : Network : Logger : Console](port: Int): F[Unit]

}

object Relay extends Relay {

  override def boostrap[F[_] : Async : Concurrent : Network : Logger : Console](port: Int): F[Unit] =
    Network[F]
      .server(port = Port.fromInt(port))
      .flatMap {
        s1 => {
          println(s"Connected to External Client on ${s1.remoteAddress}")

          s1
            .reads
            .through(text.utf8.decode)
            .map(response => {
              println(s"Response: $response")
              Console[F].println(s"Response: $response")
              response
            }).parZip(Network[F]
             .server(port = Some(port"8081"))
             .map { s2 =>
               println(s"Established relay address: localhost:8081")

              s2
                .reads
                .through(text.utf8.decode)
                .map(response => {
                  println(s"Emitting: $response")
                  response
                })
                .through(text.utf8.encode)
                .through(s1.writes)

             }
             .parJoin(100))

        }
      }
      .compile
      .drain


}
