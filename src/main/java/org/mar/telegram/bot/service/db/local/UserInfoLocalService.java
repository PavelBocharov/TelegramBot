package org.mar.telegram.bot.service.db.local;

import jakarta.annotation.PostConstruct;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.core.Ehcache;
import org.mar.telegram.bot.service.bot.db.UserService;
import org.mar.telegram.bot.service.db.dto.UserDto;
import org.mar.telegram.bot.service.jms.MQSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.logging.LogLevel;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.Random;
import java.util.UUID;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Service
@Profile("local")
public class UserInfoLocalService implements UserService {

    public static final String USER_ACTION_CACHE = UUID.randomUUID().toString();

    @Autowired
    private CacheManager cacheManager;
    @Autowired
    private MQSender mqSender;

    private Ehcache<Long, UserDto> userCache;

    @PostConstruct
    public void initCache() {
        userCache = (Ehcache<Long, UserDto>) cacheManager
                .createCache(
                        USER_ACTION_CACHE,
                        CacheConfigurationBuilder.newCacheConfigurationBuilder(
                                Long.class, UserDto.class,
                                ResourcePoolsBuilder.heap(100)
                        )
                );
    }

    @Override
    public UserDto getByUserId(String rqUuid, long userId) {
        Cache.Entry<Long, UserDto> entry = Flux.fromIterable(userCache)
                .filter(userDto -> userDto.getValue().getUserId().equals(userId))
                .blockFirst();

        UserDto user = entry == null ? null : entry.getValue();
        mqSender.sendLog(rqUuid, LogLevel.DEBUG, "Load user by userID: {}, dto: {}", userId, user);

        if (isNull(user)) {
            user = save(rqUuid, UserDto.builder().userId(userId).build());
        }

        return user;
    }

    @Override
    public UserDto save(String rqUuid, UserDto user) {
        if (nonNull(user)) {
            if (isNull(user.getId())) {
                user.setId(new Random().nextLong());
            }
            userCache.put(user.getId(), user);
        }
        mqSender.sendLog(rqUuid, LogLevel.DEBUG, "Save user: {}", user);
        return user;
    }
}
