package org.mar.telegram.bot.service.db;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mar.dto.rest.BaseRq;
import com.mar.dto.rest.BaseRs;
import lombok.SneakyThrows;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.mar.telegram.bot.service.logger.LoggerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Date;

import static com.mar.dto.mq.LogEvent.LogLevel.DEBUG;
import static com.mar.dto.mq.LogEvent.LogLevel.ERROR;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Service
public class RestApiService {

    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .build();
    @Autowired
    private LoggerService loggerService;

    @SneakyThrows
    public <RS extends BaseRs, RQ extends BaseRq> RS post(String rqUuid, String url, RQ rq, Class<RS> rsClass, String logText) {
        rq.setRqUuid(rqUuid);
        rq.setRqTm(new Date());

        String rqString = new ObjectMapper().writeValueAsString(rq);

        loggerService.sendLog(rqUuid, DEBUG, ">>> POST {}: url: {}, body: {}", logText, url, rqString);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .POST(HttpRequest.BodyPublishers.ofString(rqString, UTF_8))
                .setHeader("Content-Type", APPLICATION_JSON_VALUE)
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException(response.body());
        }

        loggerService.sendLog(rqUuid, DEBUG, "<<< POST {}: BODY {}", logText, response.body());
        RS rs = convertJsonToObject(rqUuid, rsClass, response.body());
        loggerService.sendLog(rqUuid, DEBUG, "<<< POST {}: DTO {}", logText, rs);
        return rs;
    }

    @SneakyThrows
    public <RS> RS get(String rqUuid, String url, Class<RS> rsClass, String logText, Pair<String, String>... headers) {
        loggerService.sendLog(rqUuid, DEBUG, ">>> GET {}: url: {}", logText, url);
        HttpRequest.Builder request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .setHeader("Content-Type", APPLICATION_JSON_VALUE)
                .setHeader("RqUuid", rqUuid)
                .setHeader("RqTm", String.valueOf(new Date()));

        if (headers != null) {
            for (Pair<String, String> header : headers) {
                request.setHeader(header.getKey(), header.getValue());
            }
        }

        HttpResponse<String> response = httpClient.send(request.build(), HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException(response.body());
        }
        RS rs = convertJsonToObject(rqUuid, rsClass, response.body());
        loggerService.sendLog(rqUuid, DEBUG, "<<< GET {}: {}", logText, rs);
        return rs;
    }

    private <RS> RS convertJsonToObject(String rqUuid, Class<RS> rsClass, String json) {
        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        loggerService.sendLog(rqUuid, DEBUG, "Convert JSON to Obj: JSON = '{}'", json);
        try {
            RS rs = mapper.readValue(json, rsClass);
            loggerService.sendLog(rqUuid, DEBUG, "Convert JSON to Obj: Obj = '{}'", rs);
            return rs;
        } catch (Exception e) {
            loggerService.sendLog(rqUuid, ERROR, "Convert JSON to Obj: Exception = {}", ExceptionUtils.getRootCauseMessage(e));
            return null;
        }
    }

}
