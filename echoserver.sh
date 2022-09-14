#!/bin/bash
host=$1
port=$2
echo "established relay address: $host:$port"
# Insert commands here
sbt "runMain com.bluematador.EchoServer $host $port"