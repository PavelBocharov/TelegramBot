package org.mar.telegram.bot.service.bot.db;

import org.mar.telegram.bot.service.db.dto.PostInfoDto;

public interface PostService {

    PostInfoDto getNotSendPost();
    PostInfoDto save(PostInfoDto postInfo);
    PostInfoDto getByChatIdAndMessageId(Long chatId, Integer messageId);

}
