package org.mar.telegram.bot.db.service.image;

import org.mar.telegram.bot.db.service.image.dto.UserDto;
import org.mar.telegram.bot.service.bot.db.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@Profile("local")
public class UserInfoService implements UserService {

    @Value("${application.bot.db.url}")
    private String dbUrl;

    private WebClient webClient = WebClient.create();

    public UserDto getByUserId(long userId) {
        UserDto userInfo =  webClient.get()
                .uri(dbUrl + "/user/uid/" + userId)
                .retrieve()
                .bodyToMono(UserDto.class)
                .log()
                .block();

        if (userInfo == null) {
            return save(UserDto.builder().userId(userId).build());
        }
        return userInfo;
    }

    public UserDto save(UserDto user) {
        return webClient.get()
                .uri(dbUrl+ "/user/create/uid/"+user.getUserId())
                .retrieve()
                .bodyToMono(UserDto.class)
                .log()
                .block();
    }

}
