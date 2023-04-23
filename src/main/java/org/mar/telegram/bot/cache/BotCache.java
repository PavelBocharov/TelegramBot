package org.mar.telegram.bot.cache;

public interface BotCache {

    String getFileName(Long chatId);
    void setFileName(Long chatId, String filename);

}
