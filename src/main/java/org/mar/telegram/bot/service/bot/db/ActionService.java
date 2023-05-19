package org.mar.telegram.bot.service.bot.db;

import org.mar.telegram.bot.db.entity.ActionEnum;
import org.mar.telegram.bot.db.entity.ActionPost;
import org.mar.telegram.bot.db.entity.PostInfo;
import org.mar.telegram.bot.db.entity.UserInfo;

public interface ActionService {

    ActionPost getByPostIdAndUserInfoId(PostInfo postInfo, UserInfo user);
    ActionPost save(ActionPost actionPost);
    long countByPostIdAndAction(Long postId, ActionEnum actionEnum);

}
