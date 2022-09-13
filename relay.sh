#!/bin/bash
port=$1
echo "Running relay server on $port"
# Insert commands here
sbt "runMain com.bluematador.RelayServer $port"