package org.mar.telegram.bot.service.db.docker;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.SneakyThrows;
import org.mar.telegram.bot.service.bot.db.UserService;
import org.mar.telegram.bot.service.db.RestApiService;
import org.mar.telegram.bot.service.db.dto.UserDto;
import org.mar.telegram.bot.service.jms.MQSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.logging.LogLevel;
import org.springframework.context.annotation.Profile;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.util.logging.Level;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Service
@Profile("!local")
public class UserInfoService implements UserService {


    @Value("${application.bot.db.url}")
    private String dbUrl;

    @Autowired
    private MQSender mqSender;
    @Autowired
    private RestApiService restApiService;

    public UserDto getByUserId(String rqUuid, long userId) {
        final String url = dbUrl + "/user/uid/" + userId;
        UserDto userDto = restApiService.get(rqUuid, url, UserDto.class, "getByUserId by user id " + userId);

        if (isNull(userDto) || (nonNull(userDto.getErrorCode()) && userDto.getErrorCode() > 0)) {
            userDto = create(rqUuid, new UserDto().withUserId(userId));
        }
        mqSender.sendLog(rqUuid, LogLevel.DEBUG, "Load user by userID: {}, dto: {}", userId, userDto);
        return userDto;
    }

    public UserDto create(String rqUuid, UserDto user) {
        final String url = dbUrl + "/user/create/uid/" + user.getUserId();
        UserDto userDto = restApiService.get(rqUuid, url, UserDto.class, "create user with user id " + user.getUserId());
        mqSender.sendLog(rqUuid, LogLevel.DEBUG, "Create user: {}", userDto);
        return userDto;
    }

}
