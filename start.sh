#!/usr/bin/env bash
cd target
java -server -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=8000 \
 -Dadeptj.rt.port=9007 \
 -Denable.http2=true \
 -Dlog.async=true \
 -Dlog.immediate.flush=true \
 -Dfelix.log.level=3 \
 -jar adeptj-runtime.jar