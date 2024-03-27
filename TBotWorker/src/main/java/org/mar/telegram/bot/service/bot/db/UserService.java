package org.mar.telegram.bot.service.bot.db;

import com.mar.dto.rest.UserDtoRs;

public interface UserService {
    UserDtoRs getByUserId(String rqUuid, long userId);
    UserDtoRs create(String rqUuid, UserDtoRs user);

}
