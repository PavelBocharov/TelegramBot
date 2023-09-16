package com.mar.tbot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mar.tbot.dto.*;
import com.mar.tbot.dto.sendMsg.TelegramMessage;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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

    @SneakyThrows
    protected <RS extends BaseRs, RQ extends BaseRq> RS post(String uri, RQ rq, Class<RS> rsClass, String logText) {
        rq.setRqUuid(UUID.randomUUID().toString());
        rq.setRqTm(new Date());

        ObjectMapper mapper = new ObjectMapper();

        log.debug(">>> POST {}: uri: {}, rqUuid - {}", logText, uri, rq);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(rq), UTF_8))
                .setHeader("Content-Type", APPLICATION_JSON_VALUE)
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException(response.body());
        }
        RS rs = mapper.readValue(response.body(), rsClass);
        log.debug("<<< POST {}: {}", logText, rs);
        return rs;
    }

    @SneakyThrows
    protected <RS extends BaseRs> RS get(String uri, Class<RS> rsClass, String logText) {
        ObjectMapper mapper = new ObjectMapper();

        String rqUuid = UUID.randomUUID().toString();

        log.debug(">>> GET {}: uri: {}, rqUuid - {}", logText, uri, rqUuid);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .GET()
                .setHeader("Content-Type", APPLICATION_JSON_VALUE)
                .setHeader("RqUuid", rqUuid)
                .setHeader("RqTm", String.valueOf(new Date()))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException(response.body());
        }
        RS rs = mapper.readValue(response.body(), rsClass);
        log.debug("<<< GET {}: {}", logText, rs);
        return rs;
    }

    @SneakyThrows
    protected <RS extends BaseRs, RQ extends BaseRq> RS put(String uri, RQ rq, Class<RS> rsClass, String logText) {
        rq.setRqUuid(UUID.randomUUID().toString());
        rq.setRqTm(new Date());

        ObjectMapper mapper = new ObjectMapper();

        String rqUuid = UUID.randomUUID().toString();

        log.debug(">>> PUT {}: uri: {}, rqUuid - {}", logText, uri, rqUuid);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .PUT(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(rq), UTF_8))
                .setHeader("Content-Type", APPLICATION_JSON_VALUE)
                .setHeader("RqUuid", rqUuid)
                .setHeader("RqTm", String.valueOf(new Date()))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException(response.body());
        }
        RS rs = mapper.readValue(response.body(), rsClass);
        log.debug("<<< PUT {}: {}", logText, rs);
        return rs;
    }

    @SneakyThrows
    protected <RS extends BaseRs> RS delete(String uri, long id, Class<RS> rsClass, String logText) {
        ObjectMapper mapper = new ObjectMapper();

        String rqUuid = UUID.randomUUID().toString();

        log.debug(">>> DELETE {}: uri: {}, rqUuid - {}", logText, uri, rqUuid);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .DELETE()
                .setHeader("Content-Type", APPLICATION_JSON_VALUE)
                .setHeader("RqUuid", rqUuid)
                .setHeader("RqTm", String.valueOf(new Date()))
                .setHeader("Id", String.valueOf(id))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException(response.body());
        }
        RS rs = mapper.readValue(response.body(), rsClass);
        log.debug("<<< DELETE {}: rqUuid - {}", logText, rs);
        return rs;
    }

}
