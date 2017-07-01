#!/usr/bin/env bash
java -server -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=8000 -Dadeptj.rt.port=9007 -Denable.http2=true -Dasync.logging=true -jar target/adeptj-runtime.jar