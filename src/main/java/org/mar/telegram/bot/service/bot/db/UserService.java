package org.mar.telegram.bot.service.bot.db;

import org.mar.telegram.bot.service.db.dto.UserDto;

public interface UserService {
    UserDto getByUserId(long userId);
    UserDto save(UserDto user);

}
