package org.mar.telegram.bot.service.bot.db;

import org.mar.telegram.bot.db.entity.UserInfo;

public interface UserService {

    UserInfo getByUserId(long userId);

}
