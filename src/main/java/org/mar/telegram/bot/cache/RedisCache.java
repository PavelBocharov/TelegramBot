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
@Profile("!local")
public class RedisCache implements BotCache {

    @Autowired
    private JedisPool cachePool;

    @Override
    public String getFileName(Long chatId) {
        log.debug("<< REDIS: Get file name by chat ID: {}. START...", chatId);
        try (Jedis cache = cachePool.getResource()) {
            String fileName = cache.get(getFileNameKey(chatId));
            log.debug("<< REDIS: Get file name by chat ID: {}, filename: {}. END.", chatId, fileName);
            return fileName;
        }
    }

    @Override
    public void setFileName(Long chatId, String filename) {
        log.debug(">> REDIS: Set fileName {} by chat ID: {}. START...", filename, chatId);
        try (Jedis cache = cachePool.getResource()) {
            cache.set(getFileNameKey(chatId), filename);
            log.debug(">> REDIS: Set fileName {} by chat ID: {}. END.", filename, chatId);
        }
    }

    private String getFileNameKey(Long chatId) {
        return String.format("%s::%d", FILENAME_CACHE, chatId);
    }

}
