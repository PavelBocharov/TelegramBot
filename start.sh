java -jar -Dspring.profiles.active=image /opt/app/japp.jar \
  --spring.redis.host=${REDIS_HOST}  \
  --spring.redis.port=${REDIS_PORT} \
  --logging.file.path=${LOGGER_DIR}  \
  --application.bot.token=${TELEGRAM_BOT_TOKEN} \
  --application.bot.directory.path=${IMAGE_TEMP_DIR}