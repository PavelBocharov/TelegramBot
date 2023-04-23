java -jar /opt/app/japp.jar \
  --spring.profiles.active=image \
  --spring.redis.host=${REDIS_HOST}  \
  --spring.redis.port=${REDIS_PORT} \
  --logging.file.path=${LOGGER_DIR}  \
  --application.bot.token=${TELEGRAM_BOT_TOKEN} \
  --application.bot.directory.path=${IMAGE_TEMP_DIR}