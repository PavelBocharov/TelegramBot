package org.mar.telegram.bot.service.bot.db;

import org.mar.telegram.bot.service.db.dto.PostInfoDto;

public interface PostService {

    PostInfoDto getNotSendPost(String rqUuid);
    PostInfoDto save(String rqUuid, PostInfoDto postInfo);
    PostInfoDto getByChatIdAndMessageId(String rqUuid, Long chatId, Integer messageId);

}
