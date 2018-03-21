#!/bin/sh
cd bin/
rmiregistry &
cd ..
java -classpath bin service.Peer
