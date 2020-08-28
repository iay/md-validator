#
# Dockerfile for md-validator.
#
# Performed as a two-stage build so that we can use Maven to generate the application
# but not have it (and the things it downloads) clutter up the deployed image.
#

#
# Build the .jar file in a build container.
#
FROM maven:3.5.3-jdk-11 AS build-jar
MAINTAINER Ian Young <ian@iay.org.uk>

WORKDIR /user
COPY pom.xml ./
COPY swagger swagger
COPY src src

RUN mvn --batch-mode \
    -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn \
    package

#
# Build the deployable image.
#
FROM amazoncorretto:11
MAINTAINER Ian Young <ian@iay.org.uk>

WORKDIR /user
COPY --from=build-jar /user/target/*.jar .

EXPOSE 8080
CMD ["java", "-jar", "md-validator-0.1.0-SNAPSHOT.jar"]
