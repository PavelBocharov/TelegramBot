FROM eclipse-temurin:17.0.6_10-jre-alpine

COPY target/TelegramBot*.jar /opt/app/japp.jar
COPY start.sh /opt/app/start.sh

CMD /opt/app/start.sh