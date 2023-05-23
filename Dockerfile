FROM eclipse-temurin:17.0.6_10-jre-alpine
COPY target/TelegramBot*.jar /opt/app/japp.jar
CMD ["java", "-jar", "/opt/app/japp.jar"]