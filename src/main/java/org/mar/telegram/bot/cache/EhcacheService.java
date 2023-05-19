package org.mar.telegram.bot.cache;

import org.ehcache.core.Ehcache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("local")
public class EhcacheService implements BotCache {

    @Autowired
    private Ehcache<Long, String> fileNameCache;

    @Override
    public String getFileName(Long chatId) {
        return fileNameCache.get(chatId);
    }

    @Override
    public void setFileName(Long chatId, String filename) {
        fileNameCache.put(chatId, filename);
    }

}
