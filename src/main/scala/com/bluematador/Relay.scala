package com.bluematador

import cats.effect.{Async, Concurrent, IO, Resource}
import cats.effect.std.Console
import cats.implicits.catsSyntaxFlatMapOps
import com.comcast.ip4s.{IpLiteralSyntax, Port}
import fs2.io.net.{Network, Socket}
import fs2.{Chunk, Pipe, Stream, text}
import org.http4s.HttpRoutes
import org.http4s.dsl.{Http4sDsl, RequestDslBinCompat}
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Server
import org.http4s.server.websocket.WebSocketBuilder2
import org.http4s.websocket.WebSocketFrame

import scala.concurrent.duration.DurationInt

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
          socket1    <- Network[F].server(port = Port.fromInt(port)) // System to retransmit
          _          <- Stream.eval(Console[F].println(s"Established relay address: localhost:8081"))
          socket2    <- Network[F].server(port = Some(port"8081")) // Client - ExposeThroughTcp
          //_          <- exposeThroughHttp(socket1)
          cliAddress <- Stream.eval(socket2.remoteAddress)
          _          <- Stream.eval(socket1.write(Chunk.array(s"New connection $cliAddress".getBytes))) // Notify new Client
        } yield handleRelay(socket1, socket2)) handleErrorWith { t =>
          Stream.eval(Console[F].error(s"Error: $t")).drain
        }

    private val handleRelay: (Socket[F], Socket[F]) => Stream[F, (Nothing, Nothing)] =
      (socket1, socket2) => {

        lazy val forwarding: Stream[F, Nothing] =
          socket2
            .reads
            .through(socket1.writes)

        lazy val response: Stream[F, Nothing] =
          socket1
            .reads
            .through(text.utf8.decode)
            .parEvalMap(2)(response =>
              socket2.write(Chunk.array(response.getBytes)) >>
                Console[F].println(response)
            )
            .drain

        forwarding parZip response
      }

  }

  def exposeThroughHttp[F[_] : Async : Network : Console](socket1: Socket[F]): Stream[F, Server] =
    Stream
      .resource(
        EmberServerBuilder
          .default[F]
          .withHost(host"localhost")
          .withPort(port"8082")
          .withHttpWebSocketApp(routes[F](_, socket1).orNotFound)
          .build
      )

  private def routes[F[_]: Async: Network: Console](ws: WebSocketBuilder2[F], socket1: Socket[F]): HttpRoutes[F] = {
    val dsl: Http4sDsl[F] with RequestDslBinCompat = Http4sDsl[F]
    import dsl._
    HttpRoutes
      .of[F] {
        case GET -> Root / "connect" =>

          val send: Stream[F, WebSocketFrame] =
            Stream
              .awakeEvery[F](1.second)
              .flatMap { _ =>
                socket1
                  .reads
                  .through(text.utf8.decode)
              }
              .map { r =>
                WebSocketFrame.Text(r)
              }

          val receive: Pipe[F, WebSocketFrame, Unit] =
            in => in evalMap { frameIn =>
              socket1.write(Chunk.array(frameIn.data.toArray))
            }

          ws.build(send, receive)

        case _ => NotFound("Not the path Bro")
      }
  }

}
