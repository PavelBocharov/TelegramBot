package org.mar.telegram.bot.service.bot.db;

import org.mar.telegram.bot.service.db.dto.UserDto;

public interface UserService {
    UserDto getByUserId(String rqUuid, long userId);
    UserDto save(String rqUuid, UserDto user);

}
