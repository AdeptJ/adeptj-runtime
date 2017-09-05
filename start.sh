#!/usr/bin/env bash
cd target
java -server -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=8000 \
 -Dadeptj.rt.port=9007 \
 -Dadeptj.rt.mode=PROD \
 -Denable.http2=true \
 -Dlog.async=true \
 -Dlog.immediate.flush=true \
 -Dfelix.log.level=3 \
 -Dprovision.bundles.explicitly=false \
 -Dmax.concurrent.requests=10000 \
 -Duse.supplied.keyStore=false \
 -Djavax.net.ssl.keyStore=/Users/rakesh.kumar/AdeptJRuntime.jks \
 -Djavax.net.ssl.keyStorePassword=AdeptJUndertow@Xnio \
 -Djavax.net.ssl.keyPassword=AdeptJUndertow@Xnio \
 -jar adeptj-runtime.jar