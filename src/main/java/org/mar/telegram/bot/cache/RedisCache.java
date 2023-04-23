package org.mar.telegram.bot.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import static org.mar.telegram.bot.config.CacheConf.FILENAME_CACHE;

@Slf4j
@Service
@Profile("image")
public class RedisCache implements BotCache {

    @Autowired
    private JedisPool cachePool;

    @Override
    public String getFileName(Long chatId) {
        try (Jedis cache = cachePool.getResource()) {
            return cache.get(getFileNameKey(chatId));
        }
    }

    @Override
    public void setFileName(Long chatId, String filename) {
        try (Jedis cache = cachePool.getResource()) {
            cache.set(getFileNameKey(chatId), filename);
        }
    }

    private String getFileNameKey(Long chatId) {
        return String.format("%s::%d", FILENAME_CACHE, chatId);
    }

}
