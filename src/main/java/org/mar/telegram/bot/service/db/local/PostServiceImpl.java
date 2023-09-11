package org.mar.telegram.bot.service.db.local;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.core.Ehcache;
import org.mapstruct.factory.Mappers;
import org.mar.telegram.bot.mapper.DBIntegrationMapper;
import org.mar.telegram.bot.service.bot.db.PostService;
import org.mar.telegram.bot.service.db.dto.PostInfoDtoRs;
import org.mar.telegram.bot.service.db.dto.PostInfoDtoRq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.UUID;

import static java.lang.Boolean.TRUE;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Slf4j
@Service
@Profile("local")
public class PostServiceImpl implements PostService {

    public static final String POST_ACTION_CACHE = UUID.randomUUID().toString();

    @Autowired
    private CacheManager cacheManager;

    private Ehcache<Long, PostInfoDtoRs> postInfoCache;
    private DBIntegrationMapper postInfoMapper = Mappers.getMapper(DBIntegrationMapper.class);

    @PostConstruct
    public void initCache() {
        postInfoCache = (Ehcache<Long, PostInfoDtoRs>) cacheManager
                .createCache(
                        POST_ACTION_CACHE,
                        CacheConfigurationBuilder.newCacheConfigurationBuilder(
                                Long.class, PostInfoDtoRs.class,
                                ResourcePoolsBuilder.heap(100)
                        )
                );
    }

    @Override
    public PostInfoDtoRs getNotSendPost(String rqUuid) {
        Cache.Entry<Long, PostInfoDtoRs> cacheData = null;

        for (Cache.Entry<Long, PostInfoDtoRs> postInfo : postInfoCache) {
            if (nonNull(postInfo)
                    && nonNull(postInfo.getValue())
                    && !TRUE.equals(postInfo.getValue().getIsSend())) {
                cacheData = postInfo;
                break;
            }
        }

        PostInfoDtoRs dto;
        if (isNull(cacheData) || isNull(cacheData.getValue())) {
            dto = save(rqUuid, new PostInfoDtoRs().withIsSend(false));
        } else {
            dto = cacheData.getValue();
        }

        return dto;
    }

    @Override
    public PostInfoDtoRs save(String rqUuid, PostInfoDtoRs postInfo) {
        if (nonNull(postInfo)) {
            if (isNull(postInfo.getId())) {
                postInfo.setId(new Random().nextLong());
            }
            log.debug("Save post info: {}", postInfo);
            postInfoCache.put(postInfo.getId(), postInfo);
        }
        return postInfo;
    }

    @Override
    public PostInfoDtoRs getByChatIdAndMessageId(String rqUuid, Long chatId, Integer messageId) {
        PostInfoDtoRs dto = null;

        for (Cache.Entry<Long, PostInfoDtoRs> entry : postInfoCache) {
            if (chatId.equals(entry.getValue().getChatId()) && messageId.equals(entry.getValue().getMessageId())) {
                dto = entry.getValue();
                break;
            }
        }

        return dto;
    }
}
