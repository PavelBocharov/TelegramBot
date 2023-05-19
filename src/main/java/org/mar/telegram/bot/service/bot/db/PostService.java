package org.mar.telegram.bot.service.bot.db;

import org.mar.telegram.bot.db.entity.PostInfo;

public interface PostService {

    PostInfo getNotSendPost();
    PostInfo save(PostInfo postInfo);
    PostInfo getByChatIdAndMessageId(Long chatId, Integer messageId);

}
