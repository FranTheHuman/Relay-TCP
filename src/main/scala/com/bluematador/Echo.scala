package com.bluematador

import cats.effect.Async
import cats.effect.kernel.Resource
import cats.effect.std.Console
import com.bluematador.models.SocketAddressData
import com.comcast.ip4s.SocketAddress
import fs2.Stream
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
  class EchoTcp[F[_] : Async : Console : Network] extends Echo[F] {

    override def echo(s: SocketAddressData): F[Unit] =
      Stream
        .resource(makeNetworkClient(s))
        .flatMap(s => `read/write`(s))
        .compile
        .drain

    private def makeNetworkClient(s: SocketAddressData): Resource[F, Socket[F]] =
      Network[F].client(SocketAddress(s.host, s.port))

    private def `read/write`(s: Socket[F]): Stream[F, Nothing] =
      Stream
        .eval(Console[F].println("Connected to RelayServer ..."))
        .flatMap(_ => s.reads.through(s.writes))

  }

}
