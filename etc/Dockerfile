FROM adoptopenjdk/openjdk8-openj9:latest
RUN mkdir /opt/app
COPY adeptj-runtime.jar /opt/app
CMD ["java", "-jar", "/opt/app/adeptj-runtime.jar"]
