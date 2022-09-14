#!/bin/bash
port=$1
# echo "Running relay server on $port"
sbt "runMain com.bluematador.RelayServer $port" | grep '<>'