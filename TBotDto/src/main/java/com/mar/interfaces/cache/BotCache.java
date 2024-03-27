package com.mar.interfaces.cache;

public interface BotCache {

    String getFileName(Long chatId);
    void setFileName(Long chatId, String filename);

}
