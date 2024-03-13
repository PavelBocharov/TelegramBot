FROM node:16.13.1-alpine

WORKDIR /opt/app

ADD docker_files/OpenJDK21U-jre_x64_alpine-linux_hotspot_21.0.2_13.tar.gz /opt/app/java
#JDK17 - https://adoptium.net/temurin/archive/?version=17

COPY target/*.jar /opt/app/japp.jar

ENV BOT_PROFILE=image
ENV SPRING_CLOUD_CONFIG_SERVER="configserver:http://tbotconf:8888"

CMD ["/opt/app/java/jdk-21.0.2+13-jre/bin/java", "-jar", "/opt/app/japp.jar", "--spring.profiles.active=${BOT_PROFILE}", "--spring.config.import=${SPRING_CLOUD_CONFIG_SERVER}"]

EXPOSE 443
EXPOSE 8080