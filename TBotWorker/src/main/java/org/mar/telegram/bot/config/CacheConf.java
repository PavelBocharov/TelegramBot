package org.mar.telegram.bot.config;

import com.mar.Const;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.core.Ehcache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class CacheConf {

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

}
