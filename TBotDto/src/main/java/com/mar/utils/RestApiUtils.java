package com.mar.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mar.dto.rest.BaseRq;
import com.mar.dto.rest.BaseRs;
import com.mar.exception.TbotException;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Date;

import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
@UtilityClass
public class RestApiUtils {

    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .build();


    public static <T extends BaseRs> T enrichRs(T rs, String rqUuid) {
        if (rs != null) {
            rs.setRqUuid(rqUuid);
            rs.setRqTm(new Date());
        }
        return rs;
    }

    public static String getUri(String host, Integer port) {
        return String.format("http://%s:%d", host, port);
    }

    public static <RS extends BaseRs, RQ extends BaseRq> RS post(String rqUuid, String uri, RQ rq, Class<RS> rsClass, String logText) {
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
                .setHeader("Content-Type", "application/json")
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


    public static <RS extends BaseRs> RS get(String rqUuid, String uri, Class<RS> rsClass, String logText) {
        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        Date rqTm = new Date();
        log.debug(">>> GET {}: uri: {}, rqUuid - {}", logText, uri, rqUuid);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .GET()
                .setHeader("Content-Type", "application/json")
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

    public static <RS extends BaseRs, RQ extends BaseRq> RS put(String rqUuid, String uri, RQ rq, Class<RS> rsClass, String logText) {
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
                .setHeader("Content-Type", "application/json")
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

    public static <RS extends BaseRs> RS delete(String rqUuid, String uri, long id, Class<RS> rsClass, String logText) {
        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        Date rqTm = new Date();

        log.debug(">>> DELETE {}: uri: {}, rqUuid - {}", logText, uri, rqUuid);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .DELETE()
                .setHeader("Content-Type", "application/json")
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
