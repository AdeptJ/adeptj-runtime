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

function javaVersion() {
   local ver=$(java -version 2>&1 | grep -i version | cut -d'"' -f2 | cut -d'.' -f1-2)
   if [[ ${ver} = "1."* ]]
       then
           ver=$(echo ${ver} | sed -e 's/1\.\([0-9]*\)\(.*\)/\1/; 1q')
       else
           ver=$(echo ${ver} | sed -e 's/\([0-9]*\)\(.*\)/\1/; 1q')
   fi
   echo ${ver}
}

JAVA_VERSION=$(javaVersion)

DEBUG=true

DEBUG_PORT=9000

if [ ${DEBUG} = true ]; then
	DEBUG_OPTS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=${DEBUG_PORT}"
fi

if [ ${JAVA_VERSION} -gt 9 ]; then
  GRAAL_VM_OPTS="-XX:+UnlockExperimentalVMOptions -XX:+UseJVMCICompiler"
fi

JVM_MEM_OPTS="-Xms256m -Xmx512m -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=256m"

JVM_OPTS="-server ${GRAAL_VM_OPTS} ${JVM_MEM_OPTS} ${DEBUG_OPTS}"

RESTEASY_OPTS=" -Dresteasy.allowGzip=true"

# Add the [java.xml.bind] module if Java version is greater than 8, otherwise some of the bundles will not start.
if [ ${JAVA_VERSION} -gt 8 ]; then
  JVM_OPTS="--illegal-access=warn "${JVM_OPTS}
fi

ADEPTJ_RUNTIME_OPTS="${JVM_OPTS} ${RESTEASY_OPTS}
 -Dadeptj.rt.port=9007 \
 -Dadeptj.rt.port.check=false \
 -Dadeptj.rt.mode=PROD \
 -Denable.http2=true \
 -Dtls.version=TLSv1.2 \
 -Dwebsocket.logs.tailing.delay=5000 \
 -Dwait.time.for.debug.attach=5
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
 -Dadeptj.rt.keyPassword=key-password"

cd target

java ${ADEPTJ_RUNTIME_OPTS} -jar adeptj-runtime.jar