package org.mar.telegram.bot.service.db.local;

import jakarta.annotation.PostConstruct;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.core.Ehcache;
import org.mar.telegram.bot.service.bot.db.PostService;
import org.mar.telegram.bot.service.db.dto.PostInfoDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.Random;
import java.util.UUID;

import static java.lang.Boolean.TRUE;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Service
@Profile("local")
public class PostServiceImpl implements PostService {

    public static final String POST_ACTION_CACHE = UUID.randomUUID().toString();

    @Autowired
    private CacheManager cacheManager;

    private Ehcache<Long, PostInfoDto> postInfoCache;

    @PostConstruct
    public void initCache() {
        postInfoCache = (Ehcache<Long, PostInfoDto>) cacheManager
                .createCache(
                        POST_ACTION_CACHE,
                        CacheConfigurationBuilder.newCacheConfigurationBuilder(
                                Long.class, PostInfoDto.class,
                                ResourcePoolsBuilder.heap(100)
                        )
                );
    }

    @Override
    public PostInfoDto getNotSendPost(String rqUuid) {
        Cache.Entry<Long, PostInfoDto> cacheData = Flux.fromIterable(postInfoCache)
                .filter(postInfo -> !TRUE.equals(postInfo.getValue().getIsSend()))
                .blockFirst();

        PostInfoDto dto;
        if (isNull(cacheData) || isNull(cacheData.getValue())) {
            dto = save(rqUuid, PostInfoDto.builder().isSend(false).build());
        } else {
            dto = cacheData.getValue();
        }

        return dto;
    }

    @Override
    public PostInfoDto save(String rqUuid, PostInfoDto postInfo) {
        if (nonNull(postInfo)) {
            if (isNull(postInfo.getId())) {
                postInfo.setId(new Random().nextLong());
            }
            postInfoCache.put(postInfo.getId(), postInfo);
        }
        return postInfo;
    }

    @Override
    public PostInfoDto getByChatIdAndMessageId(String rqUuid, Long chatId, Integer messageId) {
        return Flux.fromIterable(postInfoCache)
                .filter(postInfo -> postInfo.getValue().getChatId().equals(chatId)
                    && postInfo.getValue().getMessageId().equals(messageId)
                )
                .blockFirst().getValue();
    }
}
