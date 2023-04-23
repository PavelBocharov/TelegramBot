package org.mar.telegram.bot.config;

import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.core.Ehcache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@Configuration
public class CacheConf {

    public static final String FILENAME_CACHE = "chatIdMapFileName";

    @Value("${spring.redis.host:host}")
    private String redisHost;

    @Value("${spring.redis.port:0000}")
    private Integer redisPort;

    @Bean
    @Profile("!image")
    public CacheManager getEhcacheManager() {
        CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build();
        cacheManager.init();
        return cacheManager;
    }

    @Bean
    @Profile("!image")
    public Ehcache<Long, String> getFileNameCache(@Autowired CacheManager cacheManager) {
        Ehcache<Long, String> rez = (Ehcache<Long, String>) cacheManager.createCache(FILENAME_CACHE, CacheConfigurationBuilder
                .newCacheConfigurationBuilder(
                        Long.class, String.class,
                        ResourcePoolsBuilder.heap(10)
                )
        );
        return rez;
    }

    @Bean
    @Profile("image")
    public JedisPool getJedisPool() {
        return new JedisPool(new JedisPoolConfig(), redisHost, redisPort);
    }
}
