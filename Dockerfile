FROM eclipse-temurin:17.0.6_10-jre-alpine

COPY target/TelegramBot*.jar /opt/app/japp.jar
CMD ["java", "-jar", "/opt/app/japp.jar", "--spring.profiles.active=dev", "--logging.file.path=${LOGGER_DIR}", "--application.bot.token=${TELEGRAM_BOT_TOKEN}", "--application.bot.directory.path=${IMAGE_TEMP_DIR}"]