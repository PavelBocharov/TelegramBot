package com.mar.tbot.service;

import com.mar.tbot.dto.*;
import com.mar.tbot.dto.sendMsg.TelegramMessage;

public interface ApiService {
    BaseRs sendPost(PostInfoDto body);
    BaseRs sendMsg(TelegramMessage body);
    HashTagListDtoRs createHashtag(HashTagDto rq);
    HashTagListDtoRs updateHashtag(HashTagDto rq);
    HashTagListDtoRs getHashtagList();
    BaseRs removeHashtag(Long id);
    PostTypeDtoRs createPostType(PostTypeDtoRq rq);
    PostTypeDtoRs updatePostType(PostTypeDtoRq rq);
    PostTypeDtoRs removePostType(long postTypeId);
    PostTypeListDtoRs getAllPostType();

}
