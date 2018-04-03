#!/usr/bin/env bash

GREEN=$(echo -en '\033[00;32m')
RESTORE=$(echo -en '\033[0m')

# maven profiles to include support in runtime
MVN_PROFILES="Apache-HttpClient,RESTEasy,Jackson,MySQL,JDBC-CP-Hikari,JPA-EclipseLink,AWS,Ehcache,AdeptJ-Logging,ReactiveStreams"
echo ${GREEN}
echo -e "##############################################################################################################################\n"
echo -e "Building AdeptJ Runtime With Maven Profiles [$MVN_PROFILES]\n"
echo -e "Please select more profiles from pom.xml to add to the build process, if required!\n"
echo -e "##############################################################################################################################"
echo ${RESTORE}

# executing maven with profiles
mvn clean package -P ${MVN_PROFILES}