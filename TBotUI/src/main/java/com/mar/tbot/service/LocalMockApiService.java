package com.mar.tbot.service;

import com.mar.dto.rest.BaseRs;
import com.mar.dto.rest.HashTagDto;
import com.mar.dto.rest.HashTagListDtoRs;
import com.mar.dto.rest.PostTypeDtoRq;
import com.mar.dto.rest.PostTypeDtoRs;
import com.mar.dto.rest.PostTypeListDtoRs;
import com.mar.dto.rest.SendPostRq;
import com.mar.tbot.dto.sendMsg.TelegramMessageRq;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@Profile("local")
public class LocalMockApiService implements ApiService {

    @Override
    public BaseRs sendPost(SendPostRq post) {
        return new BaseRs()
                .withRqUuid(post.getRqUuid())
                .withRqTm(new Date());
    }

    @Override
    public BaseRs sendMsg(TelegramMessageRq body) {
        return new BaseRs()
                .withRqUuid(body.getRqUuid())
                .withRqTm(new Date());
    }

    @Override
    public HashTagListDtoRs createHashtag(String rqUuid, HashTagDto rq) {
        HashTagListDtoRs rs = new HashTagListDtoRs(List.of(
                new HashTagDto(1L, "#Test_teg_1"),
                new HashTagDto(2L, "#Test_teg_2"),
                new HashTagDto(3L, "#Test_teg_3")
        ));
        rs.setRqUuid(rqUuid);
        rs.setRqTm(new Date());
        return rs;
    }

    @Override
    public HashTagListDtoRs updateHashtag(String rqUuid, HashTagDto rq) {
        HashTagListDtoRs rs = new HashTagListDtoRs(List.of(
                new HashTagDto(1L, "#Test_teg_1"),
                new HashTagDto(2L, "#Test_teg_2"),
                new HashTagDto(3L, "#Test_teg_3")
        ));
        rs.setRqUuid(rqUuid);
        rs.setRqTm(new Date());
        return rs;
    }

    @Override
    public HashTagListDtoRs getHashtagList(String rqUuid) {
        HashTagListDtoRs rs = new HashTagListDtoRs(List.of(
                new HashTagDto(1L, "#Test_teg_1"),
                new HashTagDto(2L, "#Test_teg_2"),
                new HashTagDto(3L, "#Test_teg_3")
        ));
        rs.setRqUuid(rqUuid);
        rs.setRqTm(new Date());
        return rs;
    }

    @Override
    public BaseRs removeHashtag(String rqUuid, Long id) {
        return new BaseRs()
                .withRqUuid(rqUuid)
                .withRqTm(new Date());
    }

    @Override
    public PostTypeDtoRs createPostType(PostTypeDtoRq rq) {
        PostTypeDtoRs rs = new PostTypeDtoRs(rq.getId(), rq.getTitle(), rq.getLines());
        rs.setRqUuid(rq.getRqUuid());
        rs.setRqTm(new Date());
        return rs;
    }

    @Override
    public PostTypeDtoRs updatePostType(PostTypeDtoRq rq) {
        PostTypeDtoRs rs = new PostTypeDtoRs(rq.getId(), rq.getTitle(), rq.getLines());
        rs.setRqUuid(rq.getRqUuid());
        rs.setRqTm(new Date());
        return rs;
    }

    @Override
    public PostTypeDtoRs removePostType(String rqUuid, long postTypeId) {
        PostTypeDtoRs rs = new PostTypeDtoRs();
        rs.setRqUuid(rqUuid);
        rs.setRqTm(new Date());
        rs.setId(postTypeId);
        return rs;
    }

    @Override
    public PostTypeListDtoRs getAllPostType(String rqUuid) {
        PostTypeListDtoRs rs = new PostTypeListDtoRs(
                List.of(
                        new PostTypeDtoRs(1L, "Post title 1", List.of("Post 1 Line 1")),
                        new PostTypeDtoRs(2L, "Post title 2", List.of("Post 2 Line 2")),
                        new PostTypeDtoRs(3L, "Post title 3", List.of("Post 3 Line 3"))
                )
        );
        rs.setRqUuid(rqUuid);
        rs.setRqTm(new Date());
        return rs;
    }
}
