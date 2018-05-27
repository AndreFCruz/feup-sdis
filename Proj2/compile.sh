#!/bin/sh
rm -rf out/production/Proj2
rm -f *.jar
mkdir -p out/production/Proj2
javac -Xlint:unchecked -d out/production/Proj2 -sourcepath src src/service/InitPeer.java src/service/InitClient.java