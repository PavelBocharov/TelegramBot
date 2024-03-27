FROM alpine:latest

WORKDIR /opt/app

ADD docker_files/OpenJDK21U-jre_x64_alpine-linux_hotspot_21.0.2_13.tar.gz /opt/app/java

ENV BOT_PROFILE=image

COPY src/main/resources/img/forTest/github_qr.png /opt/app/fortest.png
COPY target/TBotWorker-*.jar /opt/app/japp.jar

CMD ["/opt/app/java/jdk-21.0.2+13-jre/bin/java", "-jar", "/opt/app/japp.jar", "--spring.profiles.active=${BOT_PROFILE}"]

EXPOSE 8080