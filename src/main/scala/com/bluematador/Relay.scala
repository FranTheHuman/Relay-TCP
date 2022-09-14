package com.bluematador

import cats.effect.Async
import cats.effect.std.Console
import com.comcast.ip4s.{IpLiteralSyntax, Port}
import fs2.io.net.{Network, Socket}
import fs2.text
import fs2.Stream

trait Relay[F[_]] {

  def relay(port: Int): F[Unit]

}

object Relay {

  class RelayTcp[F[_] : Async : Network : Console] extends Relay[F] {

    override def relay(port: Int): F[Unit] =
      (for {
        socket1 <- Network[F].server(port = Port.fromInt(port))
        _       <- Stream.eval(Console[F].println(s"Established relay address: localhost:8081"))
        socket2 <- Network[F].server(port = Some(port"8081"))
      } yield handleRelay(socket1, socket2)).handleErrorWith(t => {
        println(t.toString)
        fs2.Stream.empty
      }).parJoin(100)
        .compile
        .drain

    val handleRelay: (Socket[F], Socket[F]) => Stream[F, (Nothing, Nothing)] =
      (socket1, socket2) => socket2
        .reads
        .through(text.utf8.decode)
        .map(response => {
          println(s"Emitting: $response")
          response
        })
        .through(text.utf8.encode)
        .through(socket1.writes)
        .parZip(
          socket1
            .reads
            .through(text.utf8.decode)
            .map(response => {
              println(s"Response: $response")
              response
            })
            .handleErrorWith(t => {
              println(t.toString)
              fs2.Stream.empty
            }).drain
        )
        .handleErrorWith(t => {
          println(t.toString)
          fs2.Stream.empty
        })

    def relay_(port: Int): F[Unit] =
      Network[F]
        .server(port = Port.fromInt(port))
        .flatMap {
          s1 => {
            println(s"Connected to External Client on ${s1.remoteAddress}")

            Network[F]
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
                  .parZip(
                    s1
                      .reads
                      .through(text.utf8.decode)
                      .map(response => {
                        println(s"Response: $response")
                        response
                      })
                      .handleErrorWith(t => {
                        println(t.toString)
                        fs2.Stream.empty
                      }).drain
                  )
                  .handleErrorWith(t => {
                    println(t.toString)
                    fs2.Stream.empty
                  })


              }
              .parJoin(100)

          }
        }
        .handleErrorWith(t => {
          println(t.toString)
          fs2.Stream.empty
        })
        .compile
        .drain

  }

}
