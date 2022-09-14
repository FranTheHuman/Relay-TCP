package com.bluematador

import cats.effect.Async
import cats.effect.std.Console
import com.comcast.ip4s.{IpLiteralSyntax, Port}
import fs2.io.net.{Network, Socket}
import fs2.{Stream, text}

/**
 * Contract in charge of retransmitting connections between clients and servers to which it is impossible to connect
 * @tparam F Effect in charge of containing the functionalities that allow achieving the desired behavior
 */
trait Relay[F[_]] {

  /**
   * Relay behavior
   *
   * @param port - Port on which it will be waiting for invisible systems to connect
   * @return a connection between the client and the invisible system
   */
  def relay(port: Int): F[Unit]

}

object Relay {

  /**
   * Relay behavior implementation through TCP protocol
   */
  class RelayTcp[F[_] : Async : Network : Console] extends Relay[F] {

    override def relay(port: Int): F[Unit] =
      handleConnections(port)
        .parJoin(100)
        .compile
        .drain

    private val handleConnections: Int => Stream[F, Stream[F, (Nothing, Nothing)]] =
      port =>
        (for {
          socket1 <- Network[F].server(port = Port.fromInt(port)) // System to retransmit
          _       <- Stream.eval(Console[F].println(s"Established relay address: localhost:8081"))
          socket2 <- Network[F].server(port = Some(port"8081")) // Client
        } yield handleRelay(socket1, socket2)) handleErrorWith { t =>
          Stream.eval(Console[F].error(s"Error: $t")).drain
        }

    private val handleRelay: (Socket[F], Socket[F]) => Stream[F, (Nothing, Nothing)] =
      (socket1, socket2) => {

        lazy val inComing: Stream[F, Nothing] =
          socket2
            .reads
            .through(socket1.writes)

        lazy val outComing: Stream[F, Nothing] =
          socket1
            .reads
            .through(text.utf8.decode)
            .foreach(response => Console[F].println(s"$response"))
            .drain

        inComing parZip outComing
      }

  }

}
