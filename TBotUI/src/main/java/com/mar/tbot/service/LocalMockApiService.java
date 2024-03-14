package com.mar.tbot.service;

import com.mar.tbot.dto.BaseRs;
import com.mar.tbot.dto.HashTagDto;
import com.mar.tbot.dto.HashTagListDtoRs;
import com.mar.tbot.dto.PostInfoDto;
import com.mar.tbot.dto.PostTypeDtoRq;
import com.mar.tbot.dto.PostTypeDtoRs;
import com.mar.tbot.dto.PostTypeListDtoRs;
import com.mar.tbot.dto.sendMsg.TelegramMessage;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@Profile("local")
public class LocalMockApiService implements ApiService {
    @Override
    public BaseRs sendPost(PostInfoDto body) {
        return BaseRs.builder()
                .rqUuid(body.getRqUuid())
                .rqTm(new Date())
                .build();
    }

    @Override
    public BaseRs sendMsg(TelegramMessage body) {
        return BaseRs.builder()
                .rqUuid(body.getRqUuid())
                .rqTm(new Date())
                .build();
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
        return BaseRs.builder()
                .rqUuid(rqUuid)
                .rqTm(new Date())
                .build();
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
