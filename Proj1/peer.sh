#!/bin/sh
if [ "$#" -ne 2 ]; then
  echo "Usage: peer.sh <version> <peer_num>"
  exit 1
fi

java -classpath bin service.Peer "$1" "$2" //localhost/ 224.0.0.0:8000 224.0.0.0:8001 224.0.0.0:8002