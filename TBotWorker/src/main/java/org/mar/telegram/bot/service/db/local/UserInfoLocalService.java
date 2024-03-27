package org.mar.telegram.bot.service.db.local;

import com.mar.dto.rest.UserDtoRs;
import jakarta.annotation.PostConstruct;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.core.Ehcache;
import org.mar.telegram.bot.service.bot.db.UserService;
import com.mar.interfaces.mq.MQSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.UUID;

import static com.mar.dto.mq.LogEvent.LogLevel.DEBUG;
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

    private Ehcache<Long, UserDtoRs> userCache;

    @PostConstruct
    public void initCache() {
        userCache = (Ehcache<Long, UserDtoRs>) cacheManager
                .createCache(
                        USER_ACTION_CACHE,
                        CacheConfigurationBuilder.newCacheConfigurationBuilder(
                                Long.class, UserDtoRs.class,
                                ResourcePoolsBuilder.heap(100)
                        )
                );
    }

    @Override
    public UserDtoRs getByUserId(String rqUuid, long userId) {
        Cache.Entry<Long, UserDtoRs> entry = null;
        for (Cache.Entry<Long, UserDtoRs> userDto : userCache) {
            if (userDto.getValue().getUserId().equals(userId)) {
                entry = userDto;
                break;
            }
        }

        UserDtoRs user = entry == null ? null : entry.getValue();
        mqSender.sendLog(rqUuid, DEBUG, "Load user by userID: {}, dto: {}", userId, user);

        if (isNull(user)) {
            user = create(rqUuid, new UserDtoRs().withUserId(userId));
        }

        return user;
    }

    @Override
    public UserDtoRs create(String rqUuid, UserDtoRs user) {
        if (nonNull(user)) {
            if (isNull(user.getId())) {
                user.setId(new Random().nextLong());
            }
            userCache.put(user.getId(), user);
        }
        mqSender.sendLog(rqUuid, DEBUG, "Save user: {}", user);
        return user;
    }
}
