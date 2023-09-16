package org.mar.telegram.bot.service.db;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.mar.telegram.bot.controller.dto.BaseRq;
import org.mar.telegram.bot.controller.dto.BaseRs;
import org.mar.telegram.bot.service.jms.MQSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.logging.LogLevel;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Date;
import java.util.UUID;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Service
public class RestApiService {

    @Autowired
    private MQSender mqSender;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .build();


    @SneakyThrows
    public <RS extends BaseRs, RQ extends BaseRq> RS post(String rqUuid, String url, RQ rq, Class<RS> rsClass, String logText) {
        rq.setRqUuid(UUID.randomUUID().toString());
        rq.setRqTm(new Date());

        mqSender.sendLog(rqUuid, LogLevel.DEBUG, ">>> POST {}: url: {}, body: {}", logText, url, rq);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .POST(HttpRequest.BodyPublishers.ofString(new ObjectMapper().writeValueAsString(rq), UTF_8))
                .setHeader("Content-Type", APPLICATION_JSON_VALUE)
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException(response.body());
        }
        RS rs = convertJsonToObject(rqUuid, rsClass, response.body());
        mqSender.sendLog(rqUuid, LogLevel.DEBUG, "<<< POST {}: {}", logText, rs);
        return rs;
    }

    @SneakyThrows
    public <RS> RS get(String rqUuid, String url, Class<RS> rsClass, String logText) {
        mqSender.sendLog(rqUuid, LogLevel.DEBUG, ">>> GET {}: url: {}", logText, url);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .setHeader("Content-Type", APPLICATION_JSON_VALUE)
                .setHeader("RqUuid", rqUuid)
                .setHeader("RqTm", String.valueOf(new Date()))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException(response.body());
        }
        RS rs = convertJsonToObject(rqUuid, rsClass, response.body());
        mqSender.sendLog(rqUuid, LogLevel.DEBUG, "<<< GET {}: {}", logText, rs);
        return rs;
    }

    private <RS> RS convertJsonToObject(String rqUuid, Class<RS> rsClass, String json) {
        ObjectMapper mapper = new ObjectMapper();
        mqSender.sendLog(rqUuid, LogLevel.DEBUG, "Convert JSON to Obj: JSON = '{}'", json);
        try {
            RS rs = mapper.readValue(json, rsClass);
            mqSender.sendLog(rqUuid, LogLevel.DEBUG, "Convert JSON to Obj: Obj = '{}'", rs);
            return rs;
        } catch (Exception e) {
            mqSender.sendLog(rqUuid, LogLevel.ERROR, "Convert JSON to Obj: Exception = {}", ExceptionUtils.getRootCauseMessage(e));
            return null;
        }
    }

//    @SneakyThrows
//    protected <RS extends BaseRs, RQ extends BaseRq> RS put(String uri, RQ rq, Class<RS> rsClass, String logText) {
//        rq.setRqUuid(UUID.randomUUID().toString());
//        rq.setRqTm(new Date());
//
//        ObjectMapper mapper = new ObjectMapper();
//
//        String rqUuid = UUID.randomUUID().toString();
//
//        log.debug(">>> PUT {}: uri: {}, rqUuid - {}", logText, uri, rqUuid);
//        HttpRequest request = HttpRequest.newBuilder()
//                .uri(URI.create(uri))
//                .PUT(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(rq), UTF_8))
//                .setHeader("Content-Type", APPLICATION_JSON_VALUE)
//                .setHeader("RqUuid", rqUuid)
//                .setHeader("RqTm", String.valueOf(new Date()))
//                .build();
//
//        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
//
//        if (response.statusCode() != 200) {
//            throw new RuntimeException(response.body());
//        }
//        RS rs = mapper.readValue(response.body(), rsClass);
//        log.debug("<<< PUT {}: {}", logText, rs);
//        return rs;
//    }
//
//    @SneakyThrows
//    protected <RS extends BaseRs> RS delete(String uri, long id, Class<RS> rsClass, String logText) {
//        ObjectMapper mapper = new ObjectMapper();
//
//        String rqUuid = UUID.randomUUID().toString();
//
//        log.debug(">>> DELETE {}: uri: {}, rqUuid - {}", logText, uri, rqUuid);
//        HttpRequest request = HttpRequest.newBuilder()
//                .uri(URI.create(uri))
//                .DELETE()
//                .setHeader("Content-Type", APPLICATION_JSON_VALUE)
//                .setHeader("RqUuid", rqUuid)
//                .setHeader("RqTm", String.valueOf(new Date()))
//                .setHeader("Id", String.valueOf(id))
//                .build();
//
//        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
//
//        if (response.statusCode() != 200) {
//            throw new RuntimeException(response.body());
//        }
//        RS rs = mapper.readValue(response.body(), rsClass);
//        log.debug("<<< DELETE {}: rqUuid - {}", logText, rs);
//        return rs;
//    }

}
