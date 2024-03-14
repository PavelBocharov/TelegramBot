package com.mar.tbot.service;

import com.mar.tbot.dto.BaseRs;
import com.mar.tbot.dto.HashTagDto;
import com.mar.tbot.dto.HashTagListDtoRs;
import com.mar.tbot.dto.PostInfoDto;
import com.mar.tbot.dto.PostTypeDtoRq;
import com.mar.tbot.dto.PostTypeDtoRs;
import com.mar.tbot.dto.PostTypeListDtoRs;
import com.mar.tbot.dto.sendMsg.TelegramMessage;

public interface ApiService {

    BaseRs sendPost(PostInfoDto body);

    BaseRs sendMsg(TelegramMessage body);

    HashTagListDtoRs createHashtag(String rqUuid, HashTagDto rq);

    HashTagListDtoRs updateHashtag(String rqUuid, HashTagDto rq);

    HashTagListDtoRs getHashtagList(String rqUuid);

    BaseRs removeHashtag(String rqUuid, Long id);

    PostTypeDtoRs createPostType(PostTypeDtoRq rq);

    PostTypeDtoRs updatePostType(PostTypeDtoRq rq);

    PostTypeDtoRs removePostType(String rqUuid, long postTypeId);

    PostTypeListDtoRs getAllPostType(String rqUuid);

}
