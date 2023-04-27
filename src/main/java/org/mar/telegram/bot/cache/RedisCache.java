package org.mar.telegram.bot.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import static org.mar.telegram.bot.config.CacheConf.FILENAME_CACHE;

@Slf4j
@Service
@Profile("image")
public class RedisCache implements BotCache {

    @Value("${spring.redis.host}")
    private String redisHost;

    @Value("${spring.redis.port}")
    private String redisPort;

    @Autowired
    private JedisPool cachePool;

    @Override
    public String getFileName(Long chatId) {
        log.debug("<< REDIS: {}:{}, get file name by chat ID: {}. START...", redisHost, redisPort, chatId);
        try (Jedis cache = cachePool.getResource()) {
            String fileName = cache.get(getFileNameKey(chatId));
            log.debug("<< REDIS: {}:{}, get file name by chat ID: {}, filename: {}. END.", redisHost, redisPort, chatId, fileName);
            return fileName;
        }
    }

    @Override
    public void setFileName(Long chatId, String filename) {
        log.debug(">> REDIS: {}:{}, set fileName {} by chat ID: {}. START...", redisHost, redisPort, filename, chatId);
        try (Jedis cache = cachePool.getResource()) {
            cache.set(getFileNameKey(chatId), filename);
            log.debug(">> REDIS: {}:{}, set fileName {} by chat ID: {}. END.", redisHost, redisPort, filename, chatId);
        }
    }

    private String getFileNameKey(Long chatId) {
        return String.format("%s::%d", FILENAME_CACHE, chatId);
    }

}
