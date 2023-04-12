#FROM eclipse-temurin:17
FROM eclipse-temurin:17.0.6_10-jre-alpine

COPY target/TelegramBot*.jar /opt/app/japp.jar
CMD ["java", "-jar", "/opt/app/japp.jar", "-Dspring.profiles.active=production", "--application.bot.token=${TELEGRAM_BOT_TOKEN}", "--application.bot.directory.path=${IMAGE_TEMP_DIR}"]