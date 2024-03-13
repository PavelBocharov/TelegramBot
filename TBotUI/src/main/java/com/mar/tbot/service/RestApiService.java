package com.mar.tbot.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mar.tbot.dto.BaseRq;
import com.mar.tbot.dto.BaseRs;
import com.mar.tbot.dto.HashTagDto;
import com.mar.tbot.dto.HashTagListDtoRq;
import com.mar.tbot.dto.HashTagListDtoRs;
import com.mar.tbot.dto.PostInfoDto;
import com.mar.tbot.dto.PostTypeDtoRq;
import com.mar.tbot.dto.PostTypeDtoRs;
import com.mar.tbot.dto.PostTypeListDtoRs;
import com.mar.tbot.dto.sendMsg.TelegramMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@Service
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

    public BaseRs sendPost(PostInfoDto body) {
        final String uri = getUri(hostWorker, portWorker) + "/post";
        return post(uri, body, BaseRs.class, "sendPost");
    }

    public BaseRs sendMsg(TelegramMessage body) {
        final String uri = getUri(hostWorker, portWorker) + "/post/msg";
        return post(uri, body, BaseRs.class, "sendMsg");
    }

    public HashTagListDtoRs createHashtag(HashTagDto rq) {
        final String uri = getUri(hostDb, portDb) + "/hashtag";
        return post(uri, new HashTagListDtoRq(List.of(rq)), HashTagListDtoRs.class, "createHashtag");
    }

    public HashTagListDtoRs updateHashtag(HashTagDto rq) {
        final String uri = getUri(hostDb, portDb) + "/hashtag";
        return put(uri, new HashTagListDtoRq(List.of(rq)), HashTagListDtoRs.class, "updateHashtag");
    }

    public HashTagListDtoRs getHashtagList() {
        final String uri = getUri(hostDb, portDb) + "/hashtag";
        return get(uri, HashTagListDtoRs.class, "getHashtagList");
    }

    public BaseRs removeHashtag(Long id) {
        final String uri = getUri(hostDb, portDb) + "/hashtag";
        return delete(uri, id, BaseRs.class, "removeHashtag");
    }

    public PostTypeDtoRs createPostType(PostTypeDtoRq rq) {
        final String uri = getUri(hostDb, portDb) + "/post/type";
        PostTypeDtoRs rs = post(uri, rq, PostTypeDtoRs.class, "createPostType");
        return rs;
    }

    public PostTypeDtoRs updatePostType(PostTypeDtoRq rq) {
        final String uri = getUri(hostDb, portDb) + "/post/type";
        return put(uri, rq, PostTypeDtoRs.class, "updatePostType");
    }

    public PostTypeListDtoRs getAllPostType() {
        final String uri = getUri(hostDb, portDb) + "/post/type";
        return get(uri, PostTypeListDtoRs.class, "getAllPostType");
    }

    public PostTypeDtoRs removePostType(long postTypeId) {
        final String uri = getUri(hostDb, portDb) + "/post/type";
        return delete(uri, postTypeId, PostTypeDtoRs.class, "removePostType");
    }

    protected String getUri(String host, Integer port) {
        return String.format("http://%s:%d", host, port);
    }

    protected <RS extends BaseRs, RQ extends BaseRq> RS post(String uri, RQ rq, Class<RS> rsClass, String logText) {
        rq.setRqUuid(UUID.randomUUID().toString());
        rq.setRqTm(new Date());

        ObjectMapper mapper = new ObjectMapper();

        log.debug(">>> POST {}: uri: {}, rqUuid - {}", logText, uri, rq);
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


    protected <RS extends BaseRs> RS get(String uri, Class<RS> rsClass, String logText) {
        ObjectMapper mapper = new ObjectMapper();
        String rqUuid = UUID.randomUUID().toString();
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

    protected <RS extends BaseRs, RQ extends BaseRq> RS put(String uri, RQ rq, Class<RS> rsClass, String logText) {
        rq.setRqUuid(UUID.randomUUID().toString());
        rq.setRqTm(new Date());

        ObjectMapper mapper = new ObjectMapper();

        String rqUuid = UUID.randomUUID().toString();
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
                .setHeader("RqTm", String.valueOf(new Date()))
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

    protected <RS extends BaseRs> RS delete(String uri, long id, Class<RS> rsClass, String logText) {
        ObjectMapper mapper = new ObjectMapper();
        String rqUuid = UUID.randomUUID().toString();
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
