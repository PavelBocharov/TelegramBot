package org.mar.bot.integration;

import org.apache.commons.io.FileUtils;
import org.mar.bot.utils.TestUtils;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.Base58;
import org.testcontainers.utility.DockerImageName;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;

abstract public class InitContainers {

    protected static Network bridge = Network.newNetwork();

    @Container
    protected static GenericContainer<?> tbotconf = new GenericContainer<>(DockerImageName.parse(TestUtils.getPropertyStr("test.integration.config.image")))
            .withExposedPorts(TestUtils.getPropertyInt("test.integration.config.port"))
            .withNetwork(bridge)
            .withNetworkAliases(TestUtils.getPropertyStr("test.integration.config.alias"))
            .withEnv(Map.of(
                    "GIT_URL", TestUtils.getPropertyStr("test.integration.config.git.url"),
                    "PRIVATE_KEY", getPrivateKey()
            ))
            .waitingFor(
                    Wait.forHttp("/actuator/health")
                            .forStatusCode(TestUtils.getPropertyInt("test.integration.config.health.code"))
                            .forPort(TestUtils.getPropertyInt("test.integration.config.port"))
                            .withReadTimeout(Duration.ofSeconds(TestUtils.getPropertyInt("test.integration.config.health.timeout")))
            );

    @Container
    protected static GenericContainer<?> postgreSQL = new GenericContainer<>(DockerImageName.parse(TestUtils.getPropertyStr("test.integration.image")))
            .withExposedPorts(TestUtils.getPropertyInt("test.integration.port"))
            .withNetwork(bridge)
            .withNetworkAliases(TestUtils.getPropertyStr("test.integration.alias"))
            .withEnv(Map.of(
                    "POSTGRES_USER", TestUtils.getPropertyStr("test.integration.db.user"),
                    "POSTGRES_PASSWORD", TestUtils.getPropertyStr("test.integration.db.pwd"),
                    "POSTGRES_DB", TestUtils.getPropertyStr("test.integration.db")
            ));

    @Container
    protected static GenericContainer<?> tbotDb = new GenericContainer<>(
            new ImageFromDockerfile("tbotDb/test/" + Base58.randomString(16).toLowerCase() , true)
                    .withDockerfile(Path.of("../TBotDB/Dockerfile"))
//            DockerImageName.parse("marolok/telegram_db:2.0.0")
    )
            .withExposedPorts(TestUtils.getPropertyInt("test.integration.tbot.db.port"))
            .withNetwork(bridge)
            .withNetworkAliases(TestUtils.getPropertyStr("test.integration.tbot.db.alias"))
            .dependsOn(tbotconf, postgreSQL)
            .withEnv(Map.of(
                    "BOT_PROFILE", TestUtils.getPropertyStr("test.integration.tbot.db.profile"),
                    "SPRING_CLOUD_CONFIG_SERVER", TestUtils.getPropertyStr("test.integration.tbot.db.config.server")
            ));

    @Container
    protected static GenericContainer<?> zookeeper = new GenericContainer<>(DockerImageName.parse(TestUtils.getPropertyStr("test.integration.zookeeper.image")))
            .withNetwork(bridge)
            .withNetworkAliases(TestUtils.getPropertyStr("test.integration.zookeeper.alias"))
            .withEnv(Map.of(
                    "ZOOKEEPER_CLIENT_PORT", TestUtils.getPropertyStr("test.integration.zookeeper.client.port"),
                    "ZOOKEEPER_TICK_TIME", TestUtils.getPropertyStr("test.integration.zookeeper.tick.time")
            ));

    @Container
    protected static GenericContainer<?> kafka = new GenericContainer<>(DockerImageName.parse(TestUtils.getPropertyStr("test.integration.kafka.image")))
            .withNetwork(bridge)
            .withNetworkAliases(TestUtils.getPropertyStr("test.integration.kafka.alias"))
            .dependsOn(zookeeper)
            .withEnv(Map.of(
                    "KAFKA_BROKER_ID", "1",
                    "KAFKA_ZOOKEEPER_CONNECT", String.format("%s:%d", TestUtils.getPropertyStr("test.integration.zookeeper.alias"), TestUtils.getPropertyInt("test.integration.zookeeper.client.port")),
                    "KAFKA_ADVERTISED_LISTENERS", "PLAINTEXT://kafka:9092,PLAINTEXT_HOST://localhost:29092",
                    "KAFKA_LISTENER_SECURITY_PROTOCOL_MAP", "PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT",
                    "KAFKA_INTER_BROKER_LISTENER_NAME", "PLAINTEXT",
                    "KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR", "1"
            ));

    @Container
    protected static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse(TestUtils.getPropertyStr("test.integration.redis.image")))
            .withNetwork(bridge)
            .withNetworkAliases(TestUtils.getPropertyStr("test.integration.redis.alias"))
            .withCommand("redis-server --save 20 1 --loglevel warning")
            .withEnv(Map.of(
                    "ZOOKEEPER_CLIENT_PORT", "2181",
                    "ZOOKEEPER_TICK_TIME", "2000"
            ));

    @Container
    protected static GenericContainer<?> tbot =  new GenericContainer<>(
            new ImageFromDockerfile("tbot/test/" + Base58.randomString(16).toLowerCase(), true)
                    .withDockerfile(Path.of("../TBotWorker/Dockerfile"))
//            DockerImageName.parse("marolok/telegram_bot:2.0.1")
    )
            .withExposedPorts(TestUtils.getPropertyInt("test.integration.tbot.port"))
            .withNetwork(bridge)
            .withNetworkAliases(TestUtils.getPropertyStr("test.integration.tbot.alias"))
            .dependsOn(tbotconf, redis, kafka, tbotDb)
            .withEnv("BOT_PROFILE", TestUtils.getPropertyStr("test.integration.tbot.profile"));
    @Container
    protected static GenericContainer<?> tbotui = new GenericContainer<>(
            new ImageFromDockerfile("tbotui/test/" + Base58.randomString(16).toLowerCase(), true)
                    .withDockerfile(Path.of("../TBotUI/Dockerfile"))
//            DockerImageName.parse("marolok/telegram-bot-ui:1.0.2")
    )
            .withExposedPorts(TestUtils.getPropertyInt("test.integration.tbot.ui.port"))
            .withNetwork(bridge)
            .withNetworkAliases(TestUtils.getPropertyStr("test.integration.tbot.ui.alias"))
            .dependsOn(tbotconf, tbotDb, tbot)
            .withEnv("BOT_PROFILE", TestUtils.getPropertyStr("test.integration.tbot.ui.profile"))
            .waitingFor(
                    Wait.forHttp("/actuator/health")
                            .forStatusCode(200)
                            .forPort(TestUtils.getPropertyInt("test.integration.tbot.ui.port"))
                            .withReadTimeout(Duration.ofSeconds(10))
            );

    private static String getPrivateKey() {
        try {
            String filePath = TestUtils.getPropertyStr("test.integration.config.ssh.path");
            return FileUtils.readFileToString(new File(filePath), "UTF-8");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
