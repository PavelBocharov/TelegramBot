package org.mar.telegram.bot.service.bot.db;

import com.mar.dto.rest.PostInfoDtoRq;
import com.mar.dto.rest.PostInfoDtoRs;

public interface PostService {

    PostInfoDtoRs getNotSendPost(String rqUuid);

    PostInfoDtoRs getPostById(String rqUuid, Long id);

    PostInfoDtoRs save(String rqUuid, PostInfoDtoRq postInfo);

    PostInfoDtoRs getByChatIdAndMessageId(String rqUuid, Long chatId, Integer messageId);

}
