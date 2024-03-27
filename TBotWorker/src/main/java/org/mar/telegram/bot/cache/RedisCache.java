package org.mar.telegram.bot.cache;

import com.mar.Const;
import com.mar.interfaces.cache.BotCache;
import com.mar.interfaces.mq.MQSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Service
@Profile("!local")
public class RedisCache implements BotCache {

    @Autowired
    private JedisPool cachePool;
    @Autowired
    private MQSender mqSender;

    @Override
    public String getFileName(Long chatId) {
        try (Jedis cache = cachePool.getResource()) {
            String fileName = cache.get(getFileNameKey(chatId));
            return fileName;
        }
    }

    @Override
    public void setFileName(Long chatId, String filename) {
        try (Jedis cache = cachePool.getResource()) {
            cache.set(getFileNameKey(chatId), filename);
        }
    }

    private String getFileNameKey(Long chatId) {
        return String.format("%s::%d", Const.FILENAME_CACHE, chatId);
    }

}
