package com.franthehuman.models

import com.comcast.ip4s.{Hostname, IpLiteralSyntax, Port}

case class SocketAddressData(host: Hostname, port: Port)

object SocketAddressData {

  def empty: SocketAddressData =
    SocketAddressData(host"localhost", port"8080")

}