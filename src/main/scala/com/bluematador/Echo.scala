package com.bluematador

import cats.effect.Async
import cats.effect.kernel.Resource
import cats.effect.std.Console
import com.bluematador.models.SocketAddressData
import com.comcast.ip4s.SocketAddress
import fs2.Stream
import fs2.io.net.{Network, Socket}
import org.typelevel.log4cats.Logger

/**
 * Contract in charge of encapsulating the echo behavior
 */
trait Echo {

  /**
   * Echo behavior through an external relay server
   *
   * @param s - Connection data for the relay server
   * @tparam F - Effect with the behaviors necessary to achieve the expected behavior
   * @return a connection to the relay server wrapped in an effect and the implementation of the behavior
   */
  def echo[F[_] : Async : Logger : Network](s: SocketAddressData): F[Unit]

}

/**
 * Echo behavior implementation
 */
object Echo extends Echo {

  override def echo[F[_] : Async : Logger : Network](s: SocketAddressData): F[Unit] =
    Stream
      .resource(makeNetworkClient(s))
      .flatMap(s => generateEcho(s))
      .compile
      .drain

  private def makeNetworkClient[F[_] : Async : Network](s: SocketAddressData): Resource[F, Socket[F]] =
    Network[F].client(SocketAddress(s.host, s.port))

  private def generateEcho[F[_]: Logger](s: Socket[F]): Stream[F, Nothing] =
    Stream
      .eval(Logger[F].info("Connected to RelayServer ..."))
      .flatMap(_ => s.reads.through(s.writes))

}
