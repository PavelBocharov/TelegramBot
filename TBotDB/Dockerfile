FROM eclipse-temurin:17.0.6_10-jre-alpine

ENV BOT_PROFILE=image
ENV SPRING_CLOUD_CONFIG_SERVER="configserver:http://tbotconftest:8888"

COPY target/TBotDB*.jar /opt/app/japp.jar

CMD ["java", "-jar", "/opt/app/japp.jar", "--spring.profiles.active=${BOT_PROFILE}", "--spring.config.import=${SPRING_CLOUD_CONFIG_SERVER}"]

EXPOSE 8081