FROM eclipse-temurin:17-jdk-ubi10-minimal

WORKDIR /app
COPY target/guardian/guardian.jar guardian.jar
EXPOSE 8080

CMD java -Dlogback.configurationFile=${LOGBACK_FILE} -jar guardian.jar