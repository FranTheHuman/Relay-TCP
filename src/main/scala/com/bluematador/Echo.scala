package com.bluematador

import cats.effect.Temporal
import cats.effect.std.Console
import com.bluematador.models.SocketAddressData
import com.comcast.ip4s.SocketAddress
import fs2.io.net.Network
import fs2.{Chunk, text}
import org.typelevel.log4cats.Logger

trait Echo {

  def boostrap[F[_] : Temporal : Console : Network : Logger](s: SocketAddressData): F[Unit]

}

object Echo extends Echo {

  override def boostrap[F[_] : Temporal : Console : Network : Logger](s: SocketAddressData): F[Unit] =
    fs2.Stream.resource(Network[F].client(SocketAddress(s.host, s.port))).flatMap { socket =>
      println(s"Connected to RelayServer ... ")
      socket.reads
        .through(text.utf8.decode)
        .map(response => {
          println(s"ECHOOOO: $response")
          response
        })
        .through(text.utf8.encode)
        .through(socket.writes)
      //socket
      //  .reads
      //  .through(text.utf8.decode)
      //  .through(text.lines)
      //  .map(response => {
      //    println(s"ECHOOOO: $response")
      //    response
      //  })
      //  .through(text.utf8.encode)
      //  .through(socket.writes)
      //  .handleErrorWith { e =>
      //    println(s"ERROR: $e")
      //    fs2.Stream.empty
      //  }.compile.drain
    }.compile.drain

}
