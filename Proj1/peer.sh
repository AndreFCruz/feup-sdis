#!/bin/sh
if [ "$#" -ne 1 ]; then
  echo "Usage: peer.sh <peer_num>"
  exit 1
fi

java -Djava.net.preferIPv4Stack=true -classpath bin service.Peer "$1" 224.0.0.0:8000 224.0.0.0:8001 224.0.0.0:8002