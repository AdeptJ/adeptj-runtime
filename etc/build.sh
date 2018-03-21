#!/usr/bin/env bash

# maven profiles to include support in runtime
mvn_profiles="Apache-HttpClient,RESTEasy-Jackson,MySQL,JDBC-CP-Hikari,JPA-EclipseLink,AWS"

echo "Building adeptj for profiles - $mvn_profiles"

# executing maven with profiles
mvn clean package -P $mvn_profiles