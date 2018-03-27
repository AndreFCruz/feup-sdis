#!/bin/sh
rm -rf bin
rm -f *.jar
mkdir -p bin
javac -Xlint:unchecked -d bin -sourcepath src src/service/TestApp.java src/service/Peer.java