#!/bin/bash
sh  ./relay.sh 8080 &
sh  ./echoserver.sh localhost 8080