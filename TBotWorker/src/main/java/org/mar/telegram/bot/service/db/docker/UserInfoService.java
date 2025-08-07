package org.mar.telegram.bot.service.db.docker;

import com.mar.dto.rest.UserDtoRs;
import org.mar.telegram.bot.service.bot.db.UserService;
import org.mar.telegram.bot.service.db.RestApiService;
import org.mar.telegram.bot.service.logger.LoggerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import static com.mar.dto.mq.LogEvent.LogLevel.DEBUG;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Service
@Profile("!local")
public class UserInfoService implements UserService {


    @Value("${application.bot.db.url}")
    private String dbUrl;

    @Autowired
    private LoggerService loggerService;
    @Autowired
    private RestApiService restApiService;

    public UserDtoRs getByUserId(String rqUuid, long userId) {
        final String url = dbUrl + "/user/uid/" + userId;
        UserDtoRs userDto = restApiService.get(rqUuid, url, UserDtoRs.class, "getByUserId by user id " + userId);

        if (isNull(userDto) || (nonNull(userDto.getErrorCode()) && userDto.getErrorCode() > 0)) {
            userDto = create(rqUuid, new UserDtoRs().withUserId(userId));
        }
        loggerService.sendLog(rqUuid, DEBUG, "Load user by userID: {}, dto: {}", userId, userDto);
        return userDto;
    }

    public UserDtoRs create(String rqUuid, UserDtoRs user) {
        final String url = dbUrl + "/user/create/uid/" + user.getUserId();
        UserDtoRs userDto = restApiService.get(rqUuid, url, UserDtoRs.class, "create user with user id " + user.getUserId());
        loggerService.sendLog(rqUuid, DEBUG, "Create user: {}", userDto);
        return userDto;
    }

}
