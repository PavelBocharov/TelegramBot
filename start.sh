java -jar -Dspring.profiles.active=image /opt/app/japp.jar --application.bot.token="${TELEGRAM_BOT_TOKEN}" --application.group.chat.id="${GROUP_CHAT_ID}" --application.bot.db.url="${TELEGRAM_DB}"