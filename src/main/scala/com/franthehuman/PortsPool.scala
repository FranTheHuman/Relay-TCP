package com.franthehuman

trait PortsPool {

  def currentPort: Int

  def generateNext: PortsPool

}

object PortsPool {

  class PortsPoolImpl(initialPort: Int) extends PortsPool {

    def currentPort: Int = initialPort

    override def generateNext: PortsPool =
      new PortsPoolImpl(currentPort + 1)

  }

}
