package org.mar.telegram.bot.service.db.local;

import jakarta.annotation.PostConstruct;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.core.Ehcache;
import org.mar.telegram.bot.service.bot.db.ActionService;
import org.mar.telegram.bot.service.db.dto.ActionEnum;
import org.mar.telegram.bot.service.db.dto.ActionPostDto;
import org.mar.telegram.bot.service.jms.MQSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.logging.LogLevel;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Service
@Profile("local")
public class ActionServiceImpl implements ActionService {

    public static final String LOCAL_ACTION_CACHE = UUID.randomUUID().toString();

    @Autowired
    private CacheManager cacheManager;
    @Autowired
    private MQSender mqSender;

    private Ehcache<Long, ActionPostDto> actionCache;

    @PostConstruct
    public void initCache() {
        actionCache = (Ehcache<Long, ActionPostDto>) cacheManager
                .createCache(
                        LOCAL_ACTION_CACHE,
                        CacheConfigurationBuilder.newCacheConfigurationBuilder(
                                Long.class, ActionPostDto.class,
                                ResourcePoolsBuilder.heap(100)
                        )
                );
    }

    @Override
    public ActionPostDto getByPostIdAndUserInfoId(String rqUuid, Long postInfoId, Long userId) {
         Cache.Entry<Long, ActionPostDto> entry = Flux.fromIterable(actionCache)
                .filter(action ->
                        action.getValue().getPostId().equals(postInfoId)
                                && action.getValue().getUserId().equals(userId)
                )
                .blockFirst();

        ActionPostDto actionDto = entry == null ? null : entry.getValue();
        mqSender.sendLog(
                rqUuid, LogLevel.DEBUG, "Get action by postId: {} and uerId: {}. Action: {}", postInfoId, userId, actionDto
        );
        if (isNull(actionDto)) {
            actionDto = ActionPostDto.builder()
                    .postId(postInfoId)
                    .userId(userId)
                    .build();
            save(rqUuid, actionDto);
        }

        return actionDto;
    }

    @Override
    public ActionPostDto save(String rqUuid, ActionPostDto actionPost) {
        if (nonNull(actionPost)) {
            if (isNull(actionPost.getId())) {
                actionPost.setId(new Random().nextLong());
            }
            actionCache.put(actionPost.getId(), actionPost);
            mqSender.sendLog(rqUuid, LogLevel.DEBUG, "Save action: {}", actionPost);
        }
        return actionPost;
    }

    @Override
    public Map<ActionEnum, Long> countByPostIdAndAction(String rqUuid, Long postId) {
        Map<ActionEnum, Long> rez = new HashMap<>();

        rez.put(ActionEnum.FIRE_HEART, getCount(ActionEnum.FIRE_HEART, postId));
        rez.put(ActionEnum.COOL, getCount(ActionEnum.COOL, postId));
        rez.put(ActionEnum.BORING, getCount(ActionEnum.BORING, postId));
        rez.put(ActionEnum.DEVIL, getCount(ActionEnum.DEVIL, postId));
        mqSender.sendLog(rqUuid, LogLevel.DEBUG, "Get count action: {}", rez);
        return rez;
    }

    private Long getCount(ActionEnum actionEnum, Long postId) {
        return Flux.fromIterable(actionCache)
                .filter(action ->
                        action.getValue().getActionCallbackData().equals(actionEnum.getCallbackData())
                        && action.getValue().getPostId().equals(postId)
                )
                .count()
                .blockOptional()
                .orElse(0L);
    }

}
