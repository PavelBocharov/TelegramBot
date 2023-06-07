package org.mar.telegram.bot.integration;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mar.telegram.bot.service.db.dto.PostInfoDto;
import org.mar.telegram.bot.service.jms.dto.LoadFileInfo;
import org.mar.telegram.bot.service.jms.dto.URLInfo;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URISyntaxException;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.junit.jupiter.api.Assertions.*;
import static org.mar.telegram.bot.utils.Utils.whatIsUrl;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

@Testcontainers
public class IntegrationTests extends InitContainers {

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
                () -> assertTrue(telegramDb.isCreated()),
                () -> assertTrue(telegramDb.isRunning()),
                () -> assertTrue(zookeeper.isCreated()),
                () -> assertTrue(zookeeper.isRunning()),
                () -> assertTrue(kafka.isCreated()),
                () -> assertTrue(kafka.isRunning()),
                () -> assertTrue(redis.isCreated()),
                () -> assertTrue(redis.isRunning()),
                () -> assertTrue(tbot.isCreated()),
                () -> assertTrue(tbot.isRunning())
        );

        String host = tbotconf.getHost();
        Integer port = tbotconf.getMappedPort(8888);
        String url = String.format("http://%s:%d/telegram-bot/test", host, port);
        System.out.println(host + " " + port + " --> " + url);

        String xml = webClient.get()
                .uri(new URI(url))
                .retrieve()
                .bodyToMono(String.class)
                .block();

        assertTrue(isNotBlank(xml));

        Thread.sleep(10_000L);

        String tbotHost = tbot.getHost();
        int tbotPort = tbot.getMappedPort(8080);
        String tbotUrl = String.format("http://%s:%d/test", tbotHost, tbotPort);

        String image = "https://www.google.com/images/branding/googlelogo/1x/googlelogo_light_color_272x92dp.png";
        String fileName = RandomStringUtils.randomAlphanumeric(5);
        URLInfo urlInfo = whatIsUrl(image);
        LoadFileInfo fileInfo = LoadFileInfo.builder()
                .fileUrl(image)
                .saveToPath("tmp")
                .fileName(fileName)
                .typeDir(urlInfo.getContentType().getTypeDit())
                .fileType(urlInfo.getFileType())
                .mediaType(urlInfo.getContentType())
                .build();

        String rs = webClient.post()
                .uri(tbotUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(fileInfo), LoadFileInfo.class)
                .retrieve()
                .bodyToMono(String.class)
                .doOnSuccess(str ->
                        System.out.println("Telegram BOT RS: " + str)
                )
                .block();

        assertTrue(isNotBlank(rs));

        String tdbHost = telegramDb.getHost();
        int tdbPort = telegramDb.getMappedPort(8081);
        String tdbUrl = String.format("http://%s:%d/postinfo/isNotSend", tdbHost, tdbPort);

        await().untilAsserted(() -> {
            PostInfoDto postInfoDto = webClient.get()
                    .uri(tdbUrl)
                    .retrieve()
                    .bodyToMono(PostInfoDto.class)
                    .block();

            assertNotNull(postInfoDto);
            assertTrue(isNotBlank(postInfoDto.getMediaPath()));
        });
    }

}
