package com.mar.tbot.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mar.dto.rest.BaseRq;
import com.mar.dto.rest.BaseRs;
import com.mar.dto.rest.HashTagDto;
import com.mar.dto.rest.HashTagListDtoRq;
import com.mar.dto.rest.HashTagListDtoRs;
import com.mar.dto.rest.PostTypeDtoRq;
import com.mar.dto.rest.PostTypeDtoRs;
import com.mar.dto.rest.PostTypeListDtoRs;
import com.mar.dto.rest.SendPostRq;
import com.mar.exception.TbotException;
import com.mar.tbot.dto.sendMsg.TelegramMessageRq;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Date;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

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

    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .build();

    public BaseRs sendPost(SendPostRq post) {
        final String uri = getUri(hostWorker, portWorker) + "/post";
        return post(post.getRqUuid(), uri, post, BaseRs.class, "sendPost");
    }

    public BaseRs sendMsg(TelegramMessageRq body) {
        final String uri = getUri(hostWorker, portWorker) + "/post/msg";
        return post(body.getRqUuid(), uri, body, BaseRs.class, "sendMsg");
    }

    public HashTagListDtoRs createHashtag(String rqUuid, HashTagDto rq) {
        final String uri = getUri(hostDb, portDb) + "/hashtag";
        return post(rqUuid, uri, new HashTagListDtoRq(List.of(rq)), HashTagListDtoRs.class, "createHashtag");
    }

    public HashTagListDtoRs updateHashtag(String rqUuid, HashTagDto rq) {
        final String uri = getUri(hostDb, portDb) + "/hashtag";
        return put(rqUuid, uri, new HashTagListDtoRq(List.of(rq)), HashTagListDtoRs.class, "updateHashtag");
    }

    public HashTagListDtoRs getHashtagList(String rqUuid) {
        final String uri = getUri(hostDb, portDb) + "/hashtag";
        return get(rqUuid, uri, HashTagListDtoRs.class, "getHashtagList");
    }

    public BaseRs removeHashtag(String rqUuid, Long id) {
        final String uri = getUri(hostDb, portDb) + "/hashtag";
        return delete(rqUuid, uri, id, BaseRs.class, "removeHashtag");
    }

    public PostTypeDtoRs createPostType(PostTypeDtoRq rq) {
        final String uri = getUri(hostDb, portDb) + "/post/type";
        PostTypeDtoRs rs = post(rq.getRqUuid(), uri, rq, PostTypeDtoRs.class, "createPostType");
        return rs;
    }

    public PostTypeDtoRs updatePostType(PostTypeDtoRq rq) {
        final String uri = getUri(hostDb, portDb) + "/post/type";
        return put(rq.getRqUuid(), uri, rq, PostTypeDtoRs.class, "updatePostType");
    }

    public PostTypeListDtoRs getAllPostType(String rqUuid) {
        final String uri = getUri(hostDb, portDb) + "/post/type";
        return get(rqUuid, uri, PostTypeListDtoRs.class, "getAllPostType");
    }

    public PostTypeDtoRs removePostType(String rqUuid, long postTypeId) {
        final String uri = getUri(hostDb, portDb) + "/post/type";
        return delete(rqUuid, uri, postTypeId, PostTypeDtoRs.class, "removePostType");
    }

    protected String getUri(String host, Integer port) {
        return String.format("http://%s:%d", host, port);
    }

    protected <RS extends BaseRs, RQ extends BaseRq> RS post(String rqUuid, String uri, RQ rq, Class<RS> rsClass, String logText) {
        rq.setRqUuid(rqUuid);
        rq.setRqTm(new Date());

        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        log.debug(">>> POST {}: uri: {}, rqUuid - {}", logText, uri, rqUuid);
        String rqBody = null;
        try {
            rqBody = mapper.writeValueAsString(rq);
        } catch (JsonProcessingException e) {
            throw new TbotException(rq.getRqUuid(), rq.getRqTm(), String.format("Cannot mapping rqBody for POST '%s'", uri), e);
        }
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .POST(HttpRequest.BodyPublishers.ofString(rqBody, UTF_8))
                .setHeader("Content-Type", APPLICATION_JSON_VALUE)
                .build();

        HttpResponse<String> response = null;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new TbotException(rq.getRqUuid(), rq.getRqTm(), String.format("Cannot send POST '%s'", uri), e);
        }

        if (response.statusCode() != 200) {
            throw new TbotException(rq.getRqUuid(), rq.getRqTm(), String.format("Cannot send POST '%s'", uri), new Exception(response.body()));
        }
        RS rs = null;
        try {
            rs = mapper.readValue(response.body(), rsClass);
        } catch (JsonProcessingException e) {
            throw new TbotException(rq.getRqUuid(), rq.getRqTm(), String.format("Cannot read value from POST '%s'", uri), e);
        }
        log.debug("<<< POST {}: {}", logText, rs);
        return rs;
    }


    protected <RS extends BaseRs> RS get(String rqUuid, String uri, Class<RS> rsClass, String logText) {
        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        Date rqTm = new Date();
        log.debug(">>> GET {}: uri: {}, rqUuid - {}", logText, uri, rqUuid);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .GET()
                .setHeader("Content-Type", APPLICATION_JSON_VALUE)
                .setHeader("RqUuid", rqUuid)
                .setHeader("RqTm", String.valueOf(rqTm))
                .build();

        HttpResponse<String> response = null;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new TbotException(rqUuid, rqTm, String.format("Cannot send GET '%s'", uri), e);
        }

        if (response.statusCode() != 200) {
            throw new TbotException(rqUuid, rqTm, String.format("Cannot send GET '%s'", uri), new Exception(response.body()));
        }
        RS rs = null;
        try {
            rs = mapper.readValue(response.body(), rsClass);
        } catch (JsonProcessingException e) {
            throw new TbotException(rqUuid, rqTm, String.format("Cannot read value from GET '%s'", uri), e);
        }
        log.debug("<<< GET {}: {}", logText, rs);
        return rs;
    }

    protected <RS extends BaseRs, RQ extends BaseRq> RS put(String rqUuid, String uri, RQ rq, Class<RS> rsClass, String logText) {
        rq.setRqUuid(rqUuid);
        rq.setRqTm(new Date());

        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        String rqBody = null;
        try {
            rqBody = mapper.writeValueAsString(rq);
        } catch (JsonProcessingException e) {
            throw new TbotException(rq.getRqUuid(), rq.getRqTm(), String.format("Cannot mapping rqBody for PUT '%s'", uri), e);
        }
        log.debug(">>> PUT {}: uri: {}, rqUuid - {}", logText, uri, rqUuid);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .PUT(HttpRequest.BodyPublishers.ofString(rqBody, UTF_8))
                .setHeader("Content-Type", APPLICATION_JSON_VALUE)
                .setHeader("RqUuid", rqUuid)
                .setHeader("RqTm", String.valueOf(rq.getRqTm()))
                .build();

        HttpResponse<String> response = null;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new TbotException(rq.getRqUuid(), rq.getRqTm(), String.format("Cannot send PUT '%s'", uri), e);
        }

        if (response.statusCode() != 200) {
            throw new TbotException(rq.getRqUuid(), rq.getRqTm(), String.format("Cannot send PUT '%s'", uri), new Exception(response.body()));
        }
        RS rs = null;
        try {
            rs = mapper.readValue(response.body(), rsClass);
        } catch (JsonProcessingException e) {
            throw new TbotException(rq.getRqUuid(), rq.getRqTm(), String.format("Cannot read value from PUT '%s'", uri), e);
        }
        log.debug("<<< PUT {}: {}", logText, rs);
        return rs;
    }

    protected <RS extends BaseRs> RS delete(String rqUuid, String uri, long id, Class<RS> rsClass, String logText) {
        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        Date rqTm = new Date();

        log.debug(">>> DELETE {}: uri: {}, rqUuid - {}", logText, uri, rqUuid);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .DELETE()
                .setHeader("Content-Type", APPLICATION_JSON_VALUE)
                .setHeader("RqUuid", rqUuid)
                .setHeader("RqTm", String.valueOf(rqTm))
                .setHeader("Id", String.valueOf(id))
                .build();

        HttpResponse<String> response = null;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new TbotException(rqUuid, rqTm, String.format("Cannot send DELETE '%s'", uri), e);
        }

        if (response.statusCode() != 200) {
            throw new TbotException(rqUuid, rqTm, String.format("Cannot send DELETE '%s'", uri), new Exception(response.body()));
        }
        RS rs = null;
        try {
            rs = mapper.readValue(response.body(), rsClass);
        } catch (JsonProcessingException e) {
            throw new TbotException(rqUuid, rqTm, String.format("Cannot read value from DELETE '%s'", uri), e);
        }
        log.debug("<<< DELETE {}: rqUuid - {}", logText, rs);
        return rs;
    }

}
