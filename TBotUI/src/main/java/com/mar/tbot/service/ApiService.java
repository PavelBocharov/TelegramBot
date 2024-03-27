package com.mar.tbot.service;

import com.mar.dto.rest.BaseRs;
import com.mar.dto.rest.HashTagDto;
import com.mar.dto.rest.HashTagListDtoRs;
import com.mar.dto.rest.PostTypeDtoRq;
import com.mar.dto.rest.PostTypeDtoRs;
import com.mar.dto.rest.PostTypeListDtoRs;
import com.mar.dto.rest.SendPostRq;
import com.mar.tbot.dto.sendMsg.TelegramMessageRq;

public interface ApiService {

    BaseRs sendPost(SendPostRq post);

    BaseRs sendMsg(TelegramMessageRq body);

    HashTagListDtoRs createHashtag(String rqUuid, HashTagDto rq);

    HashTagListDtoRs updateHashtag(String rqUuid, HashTagDto rq);

    HashTagListDtoRs getHashtagList(String rqUuid);

    BaseRs removeHashtag(String rqUuid, Long id);

    PostTypeDtoRs createPostType(PostTypeDtoRq rq);

    PostTypeDtoRs updatePostType(PostTypeDtoRq rq);

    PostTypeDtoRs removePostType(String rqUuid, long postTypeId);

    PostTypeListDtoRs getAllPostType(String rqUuid);

}
