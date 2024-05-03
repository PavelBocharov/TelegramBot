package com.mar.tbot.service;

import com.mar.dto.rest.BaseRs;
import com.mar.dto.rest.HashTagDto;
import com.mar.dto.rest.HashTagListDtoRs;
import com.mar.dto.rest.PostInfoActionListRs;
import com.mar.dto.rest.PostInfoActionRq;
import com.mar.dto.rest.PostTypeDtoRq;
import com.mar.dto.rest.PostTypeDtoRs;
import com.mar.dto.rest.PostTypeListDtoRs;
import com.mar.dto.rest.SendPostRq;
import com.mar.tbot.dto.sendMsg.TelegramMessageRq;
import com.mar.utils.RestApiUtils;
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
                        new PostTypeDtoRs(3L, "Post title 3", List.of("Post 3 Line 3")),
                        new PostTypeDtoRs(31L, "Post title 31", List.of("Post 31 Line 31")),
                        new PostTypeDtoRs(32L, "Post title 32", List.of("Post 32 Line 32")),
                        new PostTypeDtoRs(33L, "Post title 33", List.of("Post 33 Line 33")),
                        new PostTypeDtoRs(34L, "Post title 34", List.of("Post 34 Line 34")),
//                        new PostTypeDtoRs(35L, "Post title 35", List.of("Post 35 Line 35")),
                        new PostTypeDtoRs(36L, "Post title 36", List.of("Post 36 Line 36"))
                )
        );
        rs.setRqUuid(rqUuid);
        rs.setRqTm(new Date());
        return rs;
    }

    @Override
    public PostInfoActionListRs getPostInfoActionList(PostInfoActionRq rq) {
        final String uri = RestApiUtils.getUri("localhost", 8081) + "/post/search";
        return RestApiUtils.post(rq.getRqUuid(), uri, rq, PostInfoActionListRs.class, "getPostInfoActionList");


//        PostInfoActionListRs rs = new PostInfoActionListRs(Stream
//                .generate(() -> new PostInfoActionRs(
//                                new Random().nextLong(1, 100),
//                                UUID.randomUUID().toString() + UUID.randomUUID().toString() + UUID.randomUUID().toString() + UUID.randomUUID().toString(),
//                                new Random().nextBoolean(),
//                                new Random().nextLong(1, 100),
//                                new Random().nextLong(1, 100),
//                                new Random().nextLong(1, 100),
//                                new Random().nextLong(1, 100)
//                        )
//                )
//                .limit(95)
//                .collect(Collectors.toList()),
//                95
//        );
//        rs.setRqUuid(rq.getRqUuid());
//        rs.setRqTm(new Date());
//        return rs;
    }
}
