/*
object RelayActors {

  case class NewConnection(socket2: ActorRef, clientHost: String)
  case class NewMessage(data: String, clientHost: String)

  class RelayServer(pp: PortsPool) extends Actor with ActorLogging {

    import akka.io.Tcp.Bind
    import akka.io.{IO, Tcp}
    import context.system

    var socket: ActorRef = null
    var state: scala.collection.mutable.Map[String, ActorRef] = scala.collection.mutable.Map.empty

    IO(Tcp) ! Bind(self, new InetSocketAddress("localhost", pp.currentPort))

    def receive: Receive = {

      case b@Bound(localAddress) =>
        context.parent ! b

      case CommandFailed(_: Bind) =>
        context.stop(self)

      case Received(data) =>
        log.info(s"Llegue OOOOOOO ${data.toString()}")
      //data.toString() match {
      //  case s"$data-$clientHost" =>
      //    state.getOrElse(clientHost, ActorRef.noSender) ! Write(ByteString.fromString(data))
      //  case x =>
      //    log.warning("No client address set")
      //}

      case NewConnection(socket, address) =>
        state = state += address -> socket

      case NewMessage(data, address) =>
      //socket ! Write(ByteString.fromString(s"$data-$address")) // Why loop forever ?¡

      case c@Connected(remote, local) =>
        val socket1 = sender()
        log.info(s"Established relay address: localhost:${pp.currentPort}")
        val handler = context.actorOf(Props(new ClientHandler(socket1, pp.generateNext)))
        val connection = sender()
        socket = connection
        connection ! Register(handler)

    }

  }

  class ClientHandler(socket1: ActorRef, pp: PortsPool) extends Actor with ActorLogging {

    import akka.io.Tcp.Bind
    import akka.io.{IO, Tcp}
    import context.system

    IO(Tcp) ! Bind(self, new InetSocketAddress("localhost", pp.currentPort))

    var state: Option[String] = None

    def receive: Receive = {

      case b@Bound(localAddress) =>
        context.parent ! b

      case c@Connected(remote, local) =>
        val socket2 = sender()
        socket1 ! Write(ByteString.fromString(s"New connection $remote"))
        sender() ! Register(self)
        context.parent ! NewConnection(socket2, c.remoteAddress.toString)
        state = Some(c.remoteAddress.toString)

      case Received(data) =>
        log.info(s"Llegue aaaaaaa ${String.valueOf(data)}")
        context.parent ! NewMessage(data.toString(), state.getOrElse(""))

      case PeerClosed =>
        context.stop(self)

    }
  }

}

*/