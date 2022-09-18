package com.bluematador

import cats.Applicative
import cats.effect.Async
import cats.effect.kernel.Resource
import cats.effect.std.Console
import com.bluematador.models.SocketAddressData
import com.comcast.ip4s.SocketAddress
import fs2.{Stream, text}
import fs2.io.net.{Network, Socket}

/**
 * Contract in charge of encapsulating the echo behavior
 */
trait Echo[F[_]] {

  /**
   * Echo behavior
   *
   * @param s - Connection data to echo
   * @return a connection to the relay server wrapped in an effect and the implementation of the behavior
   */
  def echo(s: SocketAddressData): F[Unit]

}

object Echo {

  /**
   * Echo behavior implementation through an external relay server with TCP protocol
   */
  class EchoTcp[F[_] : Async : Applicative : Console : Network] extends Echo[F] {

    override def echo(s: SocketAddressData): F[Unit] =
      Stream
        .resource(makeNetworkClient(s))
        .flatMap(s => `read&answer`(s))
        .compile
        .drain

    private def makeNetworkClient(s: SocketAddressData): Resource[F, Socket[F]] =
      Network[F].client(SocketAddress(s.host, s.port))

    private def `read&answer`(socket: Socket[F]): Stream[F, Nothing] =
      Stream
        .eval(socket.remoteAddress)
        .evalMap(a => Console[F].println(s"Connected to relay on $a"))
        .flatMap { _ =>
          socket
            .reads
            .through(text.utf8.decode)
            .flatMap { handleNotification(_, socket) }
        }

    private val handleNotification: (String, Socket[F]) => Stream[F, Nothing] =
      (newMsg, socket) =>
        newMsg match {
          case s"New connection $address" =>
            Stream
              .eval(Console[F].println(s"New Client Connected at $address"))
              .drain
          case a =>
            Stream
              .eval(Async[F].pure(a))
              .through(text.utf8.encode)
              .through(socket.writes)
      }
  }

}
