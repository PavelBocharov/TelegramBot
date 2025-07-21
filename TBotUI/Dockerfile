FROM node:16.13.1-alpine

WORKDIR /opt/app

ADD docker_files/OpenJDK21U-jre_x64_alpine-linux_hotspot_21.0.8_9.tar.gz /opt/app/java

COPY target/*.jar /opt/app/japp.jar

ENV BOT_PROFILE=local
ENV SPRING_CLOUD_CONFIG_SERVER="optional:configserver:http://tbotconf:8888"

CMD ["/opt/app/java/jdk-21.0.8+9-jre/bin/java", "-jar", "/opt/app/japp.jar", "--spring.profiles.active=${BOT_PROFILE}", "--spring.config.import=${SPRING_CLOUD_CONFIG_SERVER}"]

#EXPOSE 443
EXPOSE 8080