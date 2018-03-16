#!/usr/bin/env bash
cd ../target
java --add-modules java.xml.bind \
 -server -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=8000 \
 -Dadeptj.rt.port=9007 \
 -Dadeptj.rt.port.check=false \
 -Dadeptj.rt.mode=PROD \
 -Denable.http2=true \
 -Dlog.async=true \
 -Dlog.immediate.flush=true \
 -Dfelix.log.level=3 \
 -Dprovision.bundles.explicitly=false \
 -Dshutdown.wait.time=60000 \
 -Dadeptj.session.timeout=3600 \
 -Dmax.concurrent.requests=5000 \
 -Denable.req.buffering=true \
 -Dreq.buff.maxBuffers=200 \
 -Duse.provided.keyStore=false \
 -Djavax.net.ssl.keyStore=/Users/rakesh.kumar/AdeptJRuntime.jks \
 -Djavax.net.ssl.keyStorePassword=AdeptJUndertow@Xnio \
 -Djavax.net.ssl.keyPassword=AdeptJUndertow@Xnio \
 -jar adeptj-runtime.jar