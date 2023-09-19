package org.mar.telegram.bot.service.bot.db;

import org.mar.telegram.bot.service.db.dto.PostInfoDtoRs;
import org.mar.telegram.bot.service.db.dto.PostInfoDtoRq;

public interface PostService {

    PostInfoDtoRs getNotSendPost(String rqUuid);
    PostInfoDtoRs save(String rqUuid, PostInfoDtoRs postInfo);
    PostInfoDtoRs getByChatIdAndMessageId(String rqUuid, Long chatId, Integer messageId);

}
