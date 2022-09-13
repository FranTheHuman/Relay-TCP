package com.bluematador

import cats.effect.Temporal
import cats.effect.std.Console
import com.bluematador.models.SocketAddressData
import com.comcast.ip4s.SocketAddress
import fs2.io.net.Network
import fs2.text
import org.typelevel.log4cats.Logger

trait Echo {

  def boostrap[F[_] : Temporal : Console : Network : Logger](s: SocketAddressData): F[Unit]

}

object Echo extends Echo {
  override def boostrap[F[_] : Temporal : Console : Network : Logger](s: SocketAddressData): F[Unit] =
    Network[F].client(SocketAddress(s.host, s.port)).use { socket =>
      println(s"Connected to RelayServer on ${socket.remoteAddress}")
      socket
        .reads
        .through(text.utf8.decode)
        .through(text.lines)
        .map(response => {
          println(s"ECHOOOO: $response")
          response
        })
        .through(text.utf8.encode)
        .through(socket.writes)
        .handleErrorWith { e =>
          println(s"ERROR: $e")
          fs2.Stream.empty
        }.compile.drain
    }
}
