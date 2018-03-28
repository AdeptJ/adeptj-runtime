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

# script to start adeptj runtime environment.
# this will check for adeptj-runtime jar in target and start instance
# if jar is present. Otherwise mvn script will be executed and runtime
# instance will be started from target dir.
#
#
# more option to be added to define jar location and config directory
# location to save runtime caching.
#
# Options for start command:
# -d can be used to set debug mode and debug port

JAR_NAME="adeptj-runtime.jar"

# operations to perform in order to start runtime jar (java instance)
start_runtime() {
    JAVA_VERSION=$(javaVersion)

    if [ ${DEBUG} = true ]; then
        DEBUG_OPTS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=${DEBUG_PORT}"
    fi

    JVM_OPTS="-server -Xms256m -Xmx512m -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=256m ${DEBUG_OPTS}"

    RESTEASY_OPTS=" -Dresteasy.allowGzip=false"

    # Add the [java.xml.bind] module if Java version is greater than 8, otherwise some of the bundles will not start.
    if [ ${JAVA_VERSION} -gt 8 ]; then
      JVM_OPTS="--add-modules java.xml.bind "${JVM_OPTS}
    fi

    ADEPTJ_RUNTIME_OPTS="${JVM_OPTS}${RESTEASY_OPTS}
     -Dadeptj.rt.port=9007 \
     -Dadeptj.rt.port.check=false \
     -Dadeptj.rt.mode=PROD \
     -Denable.http2=true \
     -Dlog.async=true \
     -Dlog.immediate.flush=true \
     -Dfelix.log.level=3 \
     -Dbenchmark.bundle.start=true \
     -Dprovision.bundles.explicitly=false \
     -Dshutdown.wait.time=60000 \
     -Dadeptj.session.timeout=3600 \
     -Dmax.concurrent.requests=5000 \
     -Denable.req.buffering=true \
     -Dreq.buff.maxBuffers=200 \
     -Duse.provided.keyStore=false \
     -Djavax.net.ssl.keyStore=path-to-local-java-keystore \
     -Djavax.net.ssl.keyStorePassword=java-keystore-password \
     -Djavax.net.ssl.keyPassword=key-password"

#    checking current location and moving to base if not already in base
    if [[ $PWD != $BASE ]]
    then
        cd $BASE
    fi

#    starting java process from base directory
    if [ -e "$JAR_PATH" ]
    then
        java ${ADEPTJ_RUNTIME_OPTS} -jar $JAR_PATH & echo $! > runtime.pid
    fi
}

BIN_PATH=$(cd $(dirname "$0") && pwd)
BASE=$(dirname "$BIN_PATH")

# Initializing default runtime jar file from target directory
# building runtime jar if target does not exist already
init_default_jar() {
    local TARGET=$BASE"/target"

    if [[ -d "$TARGET" ]]
    then
        JAR_PATH=$TARGET"/"$JAR_NAME
     else
        cd $BASE
        ./bin/build
        cd
        JAR_PATH=$TARGET"/"$JAR_NAME
    fi

    start_runtime
}

# debug flag
DEBUG=false

# port for debug
DEBUG_PORT=8000

# reading option provided with start command.
case $2 in
    # use debug port provided in start options.
    -d)
        DEBUG=true
        DEBUG_PORT=$3
        ;;

#    More options will come here
esac

# java check and version verification
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

init_default_jar

