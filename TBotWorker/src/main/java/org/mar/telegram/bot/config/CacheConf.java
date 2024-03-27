package org.mar.telegram.bot.config;

import com.mar.Const;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.core.Ehcache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.time.Duration;

@Configuration
public class CacheConf {

    @Value("${spring.redis.host:not_local}")
    private String redisHost;

    @Value("${spring.redis.port:0000}")
    private Integer redisPort;

    @Bean
    @Profile("local")
    public CacheManager getEhcacheManager() {
        CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build();
        cacheManager.init();
        return cacheManager;
    }

    @Bean
    @Profile("local")
    public Ehcache<Long, String> getFileNameCache(@Autowired CacheManager cacheManager) {
        Ehcache<Long, String> rez = (Ehcache<Long, String>) cacheManager.createCache(Const.FILENAME_CACHE, CacheConfigurationBuilder
                .newCacheConfigurationBuilder(
                        Long.class, String.class,
                        ResourcePoolsBuilder.heap(10)
                )
        );
        return rez;
    }

    @Bean
    @Profile("!local")
    public JedisPool getJedisPool() {
        return new JedisPool(new JedisPoolConfig(), redisHost, redisPort);
    }

    @Bean
    @Primary
    public ReactiveRedisConnectionFactory reactiveRedisConnectionFactory(RedisConfiguration defaultRedisConfig) {
        LettuceClientConfiguration clientConfig = LettucePoolingClientConfiguration.builder()
                .commandTimeout(Duration.ofMillis(60000)).build();
        return new LettuceConnectionFactory(defaultRedisConfig, clientConfig);
    }

    @Bean
    public RedisConfiguration defaultRedisConfig() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(redisHost);
        config.setPort(redisPort);
        return config;
    }
}
