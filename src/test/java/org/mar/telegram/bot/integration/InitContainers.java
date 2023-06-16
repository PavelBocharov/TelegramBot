package org.mar.telegram.bot.integration;

import org.apache.commons.io.FileUtils;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;

import static org.mar.telegram.bot.utils.TestUtils.getPropertyInt;
import static org.mar.telegram.bot.utils.TestUtils.getPropertyStr;

abstract public class InitContainers {

    protected static Network bridge = Network.newNetwork();

    @Container
    protected static GenericContainer<?> tbotconf = new GenericContainer<>(DockerImageName.parse(getPropertyStr("test.integration.config.image")))
            .withExposedPorts(getPropertyInt("test.integration.config.port"))
            .withNetwork(bridge)
            .withNetworkAliases(getPropertyStr("test.integration.config.alias"))
            .withEnv(Map.of(
                    "GIT_URL", getPropertyStr("test.integration.config.git.url"),
                    "PRIVATE_KEY", getPrivateKey()
            ))
            .waitingFor(
                    Wait.forHttp("/actuator/health")
                            .forStatusCode(getPropertyInt("test.integration.config.health.code"))
                            .forPort(getPropertyInt("test.integration.config.port"))
                            .withReadTimeout(Duration.ofSeconds(getPropertyInt("test.integration.config.health.timeout")))
            );

    @Container
    protected static GenericContainer<?> postgreSQL = new GenericContainer<>(DockerImageName.parse(getPropertyStr("test.integration.image")))
            .withNetwork(bridge)
            .withNetworkAliases(getPropertyStr("test.integration.alias"))
            .withEnv(Map.of(
                    "POSTGRES_USER", getPropertyStr("test.integration.db.user"),
                    "POSTGRES_PASSWORD", getPropertyStr("test.integration.db.pwd"),
                    "POSTGRES_DB", getPropertyStr("test.integration.db")
            ));

    @Container
    protected static GenericContainer<?> telegramDb = new GenericContainer<>(DockerImageName.parse(getPropertyStr("test.integration.tbot.db.image")))
            .withExposedPorts(getPropertyInt("test.integration.tbot.db.port"))
            .withNetwork(bridge)
            .withNetworkAliases(getPropertyStr("test.integration.tbot.db.alias"))
            .dependsOn(tbotconf, postgreSQL);

    @Container
    protected static GenericContainer<?> zookeeper = new GenericContainer<>(DockerImageName.parse(getPropertyStr("test.integration.zookeeper.image")))
            .withNetwork(bridge)
            .withNetworkAliases(getPropertyStr("test.integration.zookeeper.alias"))
            .withEnv(Map.of(
                    "ZOOKEEPER_CLIENT_PORT", getPropertyStr("test.integration.zookeeper.client.port"),
                    "ZOOKEEPER_TICK_TIME", getPropertyStr("test.integration.zookeeper.tick.time")
            ));

    @Container
    protected static GenericContainer<?> kafka = new GenericContainer<>(DockerImageName.parse(getPropertyStr("test.integration.kafka.image")))
            .withNetwork(bridge)
            .withNetworkAliases(getPropertyStr("test.integration.kafka.alias"))
            .dependsOn(zookeeper)
            .withEnv(Map.of(
                    "KAFKA_BROKER_ID", "1",
                    "KAFKA_ZOOKEEPER_CONNECT", String.format("%s:%d", getPropertyStr("test.integration.zookeeper.alias"), getPropertyInt("test.integration.zookeeper.client.port")),
                    "KAFKA_ADVERTISED_LISTENERS", "PLAINTEXT://kafka:9092,PLAINTEXT_HOST://localhost:29092",
                    "KAFKA_LISTENER_SECURITY_PROTOCOL_MAP", "PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT",
                    "KAFKA_INTER_BROKER_LISTENER_NAME", "PLAINTEXT",
                    "KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR", "1"
            ));

    @Container
    protected static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse(getPropertyStr("test.integration.redis.image")))
            .withNetwork(bridge)
            .withNetworkAliases(getPropertyStr("test.integration.redis.alias"))
            .withCommand("redis-server --save 20 1 --loglevel warning")
            .withEnv(Map.of(
                    "ZOOKEEPER_CLIENT_PORT", "2181",
                    "ZOOKEEPER_TICK_TIME", "2000"
            ));

    @Container
    protected static GenericContainer<?> tbot = new GenericContainer<>(DockerImageName.parse(getPropertyStr("test.integration.tbot.image")))
            .withExposedPorts(getPropertyInt("test.integration.tbot.port"))
            .withNetwork(bridge)
            .withNetworkAliases(getPropertyStr("test.integration.tbot.alias"))
            .dependsOn(tbotconf, redis, kafka, telegramDb)
            .withEnv("BOT_PROFILE", getPropertyStr("test.integration.tbot.profile"));

    private static String getPrivateKey() {
        try {
            String filePath = getPropertyStr("test.integration.config.ssh.path");
            return FileUtils.readFileToString(new File(filePath), "UTF-8");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
