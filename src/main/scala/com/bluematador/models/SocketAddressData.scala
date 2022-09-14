package com.bluematador.models

import com.comcast.ip4s.{Hostname, IpLiteralSyntax, Port}

case class SocketAddressData(host: Hostname = host"localhost", port: Port = port"8080")
