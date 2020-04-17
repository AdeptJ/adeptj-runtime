#!/usr/bin/env bash

###############################################################################
#                                                                             #
#    Copyright 2016, AdeptJ (http://www.adeptj.com)                           #
#                                                                             #
#    Licensed under the Apache License, Version 2.0 (the "License");          #
#    you may not use this file except in compliance with the License.         #
#    You may obtain a copy of the License at                                  #
#                                                                             #
#        http://www.apache.org/licenses/LICENSE-2.0                           #
#                                                                             #
#    Unless required by applicable law or agreed to in writing, software      #
#    distributed under the License is distributed on an "AS IS" BASIS,        #
#    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. #
#    See the License for the specific language governing permissions and      #
#    limitations under the License.                                           #
#                                                                             #
###############################################################################

function findJavaVersion() {
   local ver;
   ver=$(java -version 2>&1 | grep -i version | cut -d'"' -f2 | cut -d'.' -f1-2)
   if [[ ${ver} = "1."* ]]
       then
           ver=$(echo ${ver} | sed -e 's/1\.\([0-9]*\)\(.*\)/\1/; 1q')
       else
           ver=$(echo ${ver} | sed -e 's/\([0-9]*\)\(.*\)/\1/; 1q')
   fi
   echo "${ver}"
}

JAVA_VERSION=$(findJavaVersion)

if [ "${JAVA_VERSION}" -lt 11 ]; then
  echo "AdeptJ Runtime needs Java 11 or newer!"
  exit
fi

RESTEASY_OPTS=" -Dresteasy.allowGzip=true"

JVM_MEM_OPTS="-Xms256m -Xmx512m -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=256m"

JVM_OPTS="--illegal-access=permit -server ${JVM_MEM_OPTS}"

if [ "$1" = "jpda" ] ; then
  if [ -z "$JPDA_TRANSPORT" ]; then
    JPDA_TRANSPORT="dt_socket"
  fi
  if [ -z "$JPDA_ADDRESS" ]; then
    JPDA_ADDRESS="localhost:8000"
  fi
  if [ -z "$JPDA_SUSPEND" ]; then
    JPDA_SUSPEND="n"
  fi
  if [ -z "$JPDA_OPTS" ]; then
    JPDA_OPTS="-agentlib:jdwp=transport=$JPDA_TRANSPORT,address=$JPDA_ADDRESS,server=y,suspend=$JPDA_SUSPEND"
  fi
  JVM_OPTS="$JVM_OPTS $JPDA_OPTS"
  shift
fi

ADEPTJ_RUNTIME_OPTS="${JVM_OPTS} ${RESTEASY_OPTS}
 -Dadeptj.rt.port=9007 \
 -Dadeptj.rt.https.port=8443 \
 -Dadeptj.rt.port.check=false \
 -Dadeptj.rt.mode=PROD \
 -Denable.http2=true \
 -Dtls.version=TLSv1.3 \
 -Dwebsocket.logs.tailing.delay=5000 \
 -Dwait.time.for.debug.attach=5 \
 -Dlog.async=true \
 -Dlog.immediate.flush=true \
 -Dfelix.log.level=1 \
 -Dbenchmark.bundle.start=true \
 -Dprovision.bundles.explicitly=false \
 -Dshutdown.wait.time=60000 \
 -Dadeptj.session.timeout=3600 \
 -Dmax.concurrent.requests=5000 \
 -Denable.req.buffering=true \
 -Dreq.buff.maxBuffers=200 \
 -Duse.provided.keyStore=false \
 -Dadeptj.rt.keyStore=path-to-local-java-keystore \
 -Dadeptj.rt.keyStorePassword=java-keystore-password \
 -Dadeptj.rt.keyPassword=key-password \
 -Denable.eclipselink.exceptionhandler.logging=false"

cd target || exit

java ${ADEPTJ_RUNTIME_OPTS} -jar adeptj-runtime.jar