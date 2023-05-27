package org.mar.telegram.bot.service.db.docker;

import org.mar.telegram.bot.service.bot.db.UserService;
import org.mar.telegram.bot.service.db.dto.UserDto;
import org.mar.telegram.bot.service.jms.MQSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.logging.LogLevel;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@Profile("!local")
public class UserInfoService implements UserService {

    @Value("${application.bot.db.url}")
    private String dbUrl;

    @Autowired
    private MQSender mqSender;

    private WebClient webClient = WebClient.create();

    public UserDto getByUserId(String rqUuid, long userId) {
        UserDto userInfo =  webClient.get()
                .uri(dbUrl + "/user/uid/" + userId)
                .retrieve()
                .bodyToMono(UserDto.class)
                .doOnSuccess(userDto -> mqSender.sendLog(rqUuid, LogLevel.DEBUG, "Load user by userID: {}, dto: {}", userId, userDto))
                .block();

        if (userInfo == null) {
            return save(rqUuid, UserDto.builder().userId(userId).build());
        }
        return userInfo;
    }

    public UserDto save(String rqUuid, UserDto user) {
        return webClient.get()
                .uri(dbUrl+ "/user/create/uid/"+user.getUserId())
                .retrieve()
                .bodyToMono(UserDto.class)
                .doOnSuccess(userDto -> mqSender.sendLog(rqUuid, LogLevel.DEBUG, "Save user: {}", userDto))
                .block();
    }

}
