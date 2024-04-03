package com.mar.tbot.service;

import com.mar.dto.rest.BaseRs;
import com.mar.dto.rest.HashTagDto;
import com.mar.dto.rest.HashTagListDtoRq;
import com.mar.dto.rest.HashTagListDtoRs;
import com.mar.dto.rest.PostInfoActionListRs;
import com.mar.dto.rest.PostInfoActionRq;
import com.mar.dto.rest.PostTypeDtoRq;
import com.mar.dto.rest.PostTypeDtoRs;
import com.mar.dto.rest.PostTypeListDtoRs;
import com.mar.dto.rest.SendPostRq;
import com.mar.tbot.dto.sendMsg.TelegramMessageRq;
import com.mar.utils.RestApiUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@Profile("!local")
public class RestApiService implements ApiService {

    @Value("${telegram.bot.worker.url}")
    private String hostWorker;
    @Value("${telegram.bot.worker.port}")
    private Integer portWorker;

    @Value("${telegram.bot.db.url}")
    private String hostDb;
    @Value("${telegram.bot.db.port}")
    private Integer portDb;

    public BaseRs sendPost(SendPostRq post) {
        final String uri = RestApiUtils.getUri(hostWorker, portWorker) + "/post";
        return RestApiUtils.post(post.getRqUuid(), uri, post, BaseRs.class, "sendPost");
    }

    public BaseRs sendMsg(TelegramMessageRq body) {
        final String uri = RestApiUtils.getUri(hostWorker, portWorker) + "/post/msg";
        return RestApiUtils.post(body.getRqUuid(), uri, body, BaseRs.class, "sendMsg");
    }

    public HashTagListDtoRs createHashtag(String rqUuid, HashTagDto rq) {
        final String uri = RestApiUtils.getUri(hostDb, portDb) + "/hashtag";
        return RestApiUtils.post(rqUuid, uri, new HashTagListDtoRq(List.of(rq)), HashTagListDtoRs.class, "createHashtag");
    }

    public HashTagListDtoRs updateHashtag(String rqUuid, HashTagDto rq) {
        final String uri = RestApiUtils.getUri(hostDb, portDb) + "/hashtag";
        return RestApiUtils.put(rqUuid, uri, new HashTagListDtoRq(List.of(rq)), HashTagListDtoRs.class, "updateHashtag");
    }

    public HashTagListDtoRs getHashtagList(String rqUuid) {
        final String uri = RestApiUtils.getUri(hostDb, portDb) + "/hashtag";
        return RestApiUtils.get(rqUuid, uri, HashTagListDtoRs.class, "getHashtagList");
    }

    public BaseRs removeHashtag(String rqUuid, Long id) {
        final String uri = RestApiUtils.getUri(hostDb, portDb) + "/hashtag";
        return RestApiUtils.delete(rqUuid, uri, id, BaseRs.class, "removeHashtag");
    }

    public PostTypeDtoRs createPostType(PostTypeDtoRq rq) {
        final String uri = RestApiUtils.getUri(hostDb, portDb) + "/post/type";
        PostTypeDtoRs rs = RestApiUtils.post(rq.getRqUuid(), uri, rq, PostTypeDtoRs.class, "createPostType");
        return rs;
    }

    public PostTypeDtoRs updatePostType(PostTypeDtoRq rq) {
        final String uri = RestApiUtils.getUri(hostDb, portDb) + "/post/type";
        return RestApiUtils.put(rq.getRqUuid(), uri, rq, PostTypeDtoRs.class, "updatePostType");
    }

    public PostTypeListDtoRs getAllPostType(String rqUuid) {
        final String uri = RestApiUtils.getUri(hostDb, portDb) + "/post/type";
        return RestApiUtils.get(rqUuid, uri, PostTypeListDtoRs.class, "getAllPostType");
    }

    @Override
    public PostInfoActionListRs getPostInfoActionList(PostInfoActionRq rq) {
        final String uri = RestApiUtils.getUri(hostDb, portDb) + "/post/search";
        return RestApiUtils.post(rq.getRqUuid(), uri, rq, PostInfoActionListRs.class, "getPostInfoActionList");
    }

    public PostTypeDtoRs removePostType(String rqUuid, long postTypeId) {
        final String uri = RestApiUtils.getUri(hostDb, portDb) + "/post/type";
        return RestApiUtils.delete(rqUuid, uri, postTypeId, PostTypeDtoRs.class, "removePostType");
    }


}
