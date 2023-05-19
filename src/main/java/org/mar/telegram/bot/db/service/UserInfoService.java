package org.mar.telegram.bot.db.service;

import org.mar.telegram.bot.db.entity.UserInfo;
import org.mar.telegram.bot.db.jpa.UserInfoRepository;
import org.mar.telegram.bot.service.bot.db.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import static java.util.Objects.isNull;

@Service
@Profile("local")
public class UserInfoService implements UserService {

    @Autowired
    private UserInfoRepository userInfoRepository;

    public UserInfo getByUserId(long userId) {
        UserInfo userInfo = userInfoRepository.getByUserId(userId);

        if (isNull(userInfo)) {
            userInfo = userInfoRepository.save(
                    UserInfo.builder().userId(userId).build()
            );
        }

        return userInfo;
    }


}
