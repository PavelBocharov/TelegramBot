package org.mar.bot.integration;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mar.bot.integration.dto.SendPostDto;
import org.mar.bot.utils.TestUtils;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

@Slf4j
@Testcontainers
public class SendPostTest extends InitContainers {

    protected static WebClient webClient = WebClient.create();

    @BeforeAll
    public static void init() {
    }

    @Test
    void initContainers_test() throws URISyntaxException, InterruptedException {
        assertAll(
                () -> assertTrue(tbotconf.isCreated(), "tbotconf.isCreated()"),
                () -> assertTrue(tbotconf.isRunning(), "tbotconf.isRunning()"),
                () -> assertTrue(postgreSQL.isCreated(), "postgreSQL.isCreated()"),
                () -> assertTrue(postgreSQL.isRunning(), "postgreSQL.isRunning()"),
                () -> assertTrue(tbotDb.isCreated(), "tbotDb.isCreated()"),
                () -> assertTrue(tbotDb.isRunning(), "tbotDb.isRunning()"),
                () -> assertTrue(zookeeper.isCreated(), "zookeeper.isCreated()"),
                () -> assertTrue(zookeeper.isRunning(), "zookeeper.isRunning()"),
                () -> assertTrue(kafka.isCreated(), "kafka.isCreated()"),
                () -> assertTrue(kafka.isRunning(), "kafka.isRunning()"),
                () -> assertTrue(redis.isCreated(), "redis.isCreated()"),
                () -> assertTrue(redis.isRunning(), "redis.isRunning()"),
                () -> assertTrue(tbot.isCreated(), "tbot.isCreated()"),
                () -> assertTrue(tbot.isRunning(), "tbot.isRunning()"),
                () -> assertTrue(tbotui.isCreated(), "tbotui.isCreated()"),
                () -> assertTrue(tbotui.isRunning(), "tbotui.isRunning()")
        );

        tbot.followOutput(new Slf4jLogConsumer(log).withPrefix("tbot"));
        tbotDb.followOutput(new Slf4jLogConsumer(log).withPrefix("tbotDb"));

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
        sendPost.setFilePath("/opt/app/fortest.png"); //look in TBotWorker/Dockerfile
        // https://core.telegram.org/bots/api#html-style
        sendPost.setCaption(Map.of(
                "<u>Test '@'</u>", "@CrzCat",
                "<b>Test HTML link 1</b>", "<a href=\"https://github.com/PavelBocharov/TelegramBot\">TelegramBot GitHub</a>",
                "<b>Test HTML link 2</b>", "<a href=\"https://github.com/PavelBocharov/TelegramBot\">TelegramBot GitHub</a>",
                "<b>Test HTML link 3</b>", "<a href=\"https://github.com/PavelBocharov/TelegramBot\">TelegramBot GitHub</a>",
                "<b>Test HTML link 4</b>", "<a href=\"https://github.com/PavelBocharov/TelegramBot\">TelegramBot GitHub</a>"
        ));
        sendPost.setHashTags(List.of("#Test", "#IT_Test"));


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

        assertCountRow();
    }

    @SneakyThrows
    private void assertCountRow() {
        Connection connection = getDBConnection();
        Statement statement = connection.createStatement();
        ResultSet rez = statement.executeQuery("SELECT id, is_send FROM public.post_info");
        int cnt = 0;
        while (rez.next()) {
            long id = rez.getLong("id");
            boolean isSend = rez.getBoolean("is_send");
            System.out.printf("Select row in DB - id: %d, is_send: %s\n", id, String.valueOf(isSend));
            assertTrue(isSend);
            cnt++;
        }
        assertEquals(cnt, 1);
        connection.close();
    }

    @SneakyThrows
    private Connection getDBConnection() {
        String dbHost = postgreSQL.getHost();
        int dbPort = postgreSQL.getMappedPort(TestUtils.getPropertyInt("test.integration.port"));
        String db = TestUtils.getPropertyStr("test.integration.db");
        String dbUsr = TestUtils.getPropertyStr("test.integration.db.user");
        String dbPwd = TestUtils.getPropertyStr("test.integration.db.pwd");

        String connDB = String.format("jdbc:postgresql://%s:%d/%s", dbHost, dbPort, db);
        System.out.printf("!!! getDBData: '%s', %s, %s\n", connDB, dbUsr, dbPwd);

        return DriverManager.getConnection(connDB, dbUsr, dbPwd);
    }

}
