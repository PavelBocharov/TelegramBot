FROM node:16.13.1-alpine

WORKDIR /opt/app

ADD docker_files/OpenJDK17U-jre_x64_alpine-linux_hotspot_17.0.8_7.tar.gz /opt/app/java
#JDK17 - https://adoptium.net/temurin/archive/?version=17

COPY target/*.jar /opt/app/japp.jar

ENV BOT_PROFILE=image
ENV SPRING_CLOUD_CONFIG_SERVER="configserver:http://tbotconf:8888"

CMD ["/opt/app/java/jdk-17.0.8+7-jre/bin/java", "-jar", "/opt/app/japp.jar", "--spring.profiles.active=${BOT_PROFILE}", "--spring.config.import=${SPRING_CLOUD_CONFIG_SERVER}"]

EXPOSE 8080