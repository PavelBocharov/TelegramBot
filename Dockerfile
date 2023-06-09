FROM eclipse-temurin:17.0.6_10-jre-alpine
ENV BOT_PROFILE=image
COPY target/TelegramBot*.jar /opt/app/japp.jar
CMD ["java", "-jar", "/opt/app/japp.jar", "--spring.profiles.active=${BOT_PROFILE}"]