package org.mar.bot.integration;

import org.mar.bot.integration.dto.PostInfoDtoRsDto;
import org.mar.bot.integration.dto.SendPostDto;
import org.mar.bot.utils.TestUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.junit.jupiter.api.Assertions.*;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

@Testcontainers
public class SendPostTest extends InitContainers {

    protected static WebClient webClient = WebClient.create();

    @BeforeAll
    public static void init() {
    }

    @Test
    void initContainers_test() throws URISyntaxException, InterruptedException {
        assertAll(
                () -> assertTrue(tbotconf.isCreated()),
                () -> assertTrue(tbotconf.isRunning()),
                () -> assertTrue(postgreSQL.isCreated()),
                () -> assertTrue(postgreSQL.isRunning()),
                () -> assertTrue(tbotDb.isCreated()),
                () -> assertTrue(tbotDb.isRunning()),
                () -> assertTrue(zookeeper.isCreated()),
                () -> assertTrue(zookeeper.isRunning()),
                () -> assertTrue(kafka.isCreated()),
                () -> assertTrue(kafka.isRunning()),
                () -> assertTrue(redis.isCreated()),
                () -> assertTrue(redis.isRunning()),
                () -> assertTrue(tbot.isCreated()),
                () -> assertTrue(tbot.isRunning()),
                () -> assertTrue(tbotui.isCreated()),
                () -> assertTrue(tbotui.isRunning())
        );

        String host = tbotconf.getHost();
        Integer port = tbotconf.getMappedPort(TestUtils.getPropertyInt("test.integration.config.port"));
        String url = String.format("http://%s:%d/telegram-bot/test", host, port);
        System.out.println("TBotWorker test API: " + host + ":" + port + " --> " + url);

        String xml = webClient.get()
                .uri(new URI(url))
                .retrieve()
                .bodyToMono(String.class)
                .block();

        assertTrue(isNotBlank(xml));

        Thread.sleep(TestUtils.getPropertyInt("test.integration.kafka.start.sleep"));

        String tbotHost = tbot.getHost();
        int tbotPort = tbot.getMappedPort(TestUtils.getPropertyInt("test.integration.tbot.port"));
        String tbotUrl = String.format("http://%s:%d/post", tbotHost, tbotPort);

        String rqUuid = UUID.randomUUID().toString();
        SendPostDto sendPost = new SendPostDto();
        sendPost.setRqTm(new Date());
        sendPost.setRqUuid(rqUuid);
        sendPost.setUserId(TestUtils.getPropertyLong("test.integration.tbot.admin.userId"));
        sendPost.setFilePath("/opt/app/fortest.jpg"); //look in TBotWorker/Dockerfile
        sendPost.setCaption(Map.of("test_title", "test_caption"));
        sendPost.setHashTags(List.of("#test", "#it_test"));


        String rs = webClient.post()
                .uri(tbotUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(sendPost), SendPostDto.class)
                .retrieve()
                .bodyToMono(String.class)
                .doOnSuccess(str ->
                        System.out.println("Telegram BOT RS: " + str)
                )
                .block();

        assertTrue(isNotBlank(rs));

        String tdbHost = tbotDb.getHost();
        int tdbPort = tbotDb.getMappedPort(TestUtils.getPropertyInt("test.integration.tbot.db.port"));
        String tdbUrl = String.format("http://%s:%d/post/info/isNotSend", tdbHost, tdbPort);

        System.out.println("TBotDB get not send post API: " + tdbHost + ":" + tdbPort + " --> " + tdbUrl);

        await().untilAsserted(() -> {
            PostInfoDtoRsDto postInfoDto = webClient.get()
                    .uri(tdbUrl)
                    .headers(httpHeaders -> {
                        httpHeaders.add("RqUuid", UUID.randomUUID().toString());
                        httpHeaders.add("RqTm", new Date().toString());
                    })
                    .retrieve()
                    .bodyToMono(PostInfoDtoRsDto.class)
                    .doOnSuccess(postInfoDtoRs -> System.out.println("HTTP 200. Null is good :) -> RS: " + postInfoDtoRs))
                    .block();

            System.out.println("TBotDB RS: " + postInfoDto);

            assertNull(postInfoDto);
        });

        String tbotUiHost = tbotui.getHost();
        int tbotUiPort = tbotui.getMappedPort(TestUtils.getPropertyInt("test.integration.tbot.ui.port"));
        String tbotuiUrl = String.format("http://%s:%d/actuator/health", tbotUiHost, tbotUiPort);

        System.out.println("TBotDB get not send post API: " + tbotUiHost + ":" + tbotUiPort + " --> " + tbotuiUrl);
//       /actuator/health -->  {"status":"UP"}

        await().untilAsserted(() -> {
            String tbotUiHealth = webClient.get()
                    .uri(tbotuiUrl)
                    .retrieve()
                    .bodyToMono(String.class)
                    .doOnSuccess(s -> System.out.println("HTTP 200. -> RS: " + s))
                    .block();

            System.out.println("TBotUI health RS: " + tbotUiHealth);

            assertNotNull(tbotUiHealth);
            assertTrue(isNotBlank(tbotUiHealth));
            assertEquals("{\"status\":\"UP\"}", tbotUiHealth);
        });
    }

}
