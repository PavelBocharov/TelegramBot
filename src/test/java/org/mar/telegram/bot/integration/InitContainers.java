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

public class InitContainers {

    protected static Network bridge = Network.newNetwork();

    @Container
    protected static GenericContainer<?> tbotconf = new GenericContainer<>(DockerImageName.parse("marolok/telegram_conf:1.2.0"))
            .withExposedPorts(8888)
            .withNetwork(bridge)
            .withNetworkAliases("tbotconf")
            .withEnv(Map.of(
                    "GIT_URL", "git@github.com:PavelBocharov/TelegramConf.git",
                    "PRIVATE_KEY", getPrivateKey()
            ))
            .waitingFor(
                    Wait.forHttp("/actuator/health")
                            .forStatusCode(200)
                            .forPort(8888)
                            .withReadTimeout(Duration.ofSeconds(20))
            );

    @Container
    protected static GenericContainer<?> postgreSQL = new GenericContainer<>(DockerImageName.parse("postgres:14"))
            .withNetwork(bridge)
            .withNetworkAliases("pgsql")
            .withEnv(Map.of(
                    "POSTGRES_USER", "tdb",
                    "POSTGRES_PASSWORD", "tdb",
                    "POSTGRES_DB", "tdb"
            ));

    @Container
    protected static GenericContainer<?> telegramDb = new GenericContainer<>(DockerImageName.parse("marolok/telegram_db:1.1.1"))
            .withExposedPorts(8081)
            .withNetwork(bridge)
            .withNetworkAliases("tbotdb")
            .dependsOn(tbotconf, postgreSQL);

    @Container
    protected static GenericContainer<?> zookeeper = new GenericContainer<>(DockerImageName.parse("confluentinc/cp-zookeeper:latest"))
            .withNetwork(bridge)
            .withNetworkAliases("zookeeper")
            .withEnv(Map.of(
                    "ZOOKEEPER_CLIENT_PORT", "2181",
                    "ZOOKEEPER_TICK_TIME", "2000"
            ));

    @Container
    protected static GenericContainer<?> kafka = new GenericContainer<>(DockerImageName.parse("confluentinc/cp-kafka:latest"))
            .withNetwork(bridge)
            .withNetworkAliases("kafka")
            .dependsOn(zookeeper)
            .withEnv(Map.of(
                    "KAFKA_BROKER_ID", "1",
                    "KAFKA_ZOOKEEPER_CONNECT", "zookeeper:2181",
                    "KAFKA_ADVERTISED_LISTENERS", "PLAINTEXT://kafka:9092,PLAINTEXT_HOST://localhost:29092",
                    "KAFKA_LISTENER_SECURITY_PROTOCOL_MAP", "PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT",
                    "KAFKA_INTER_BROKER_LISTENER_NAME", "PLAINTEXT",
                    "KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR", "1"
            ));

    @Container
    protected static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:latest"))
            .withNetwork(bridge)
            .withNetworkAliases("redis")
            .withCommand("redis-server --save 20 1 --loglevel warning")
            .withEnv(Map.of(
                    "ZOOKEEPER_CLIENT_PORT", "2181",
                    "ZOOKEEPER_TICK_TIME", "2000"
            ));

    @Container
    protected static GenericContainer<?> tbot = new GenericContainer<>(DockerImageName.parse("marolok/telegram_bot:1.5.0"))
            .withExposedPorts(8080)
            .withNetwork(bridge)
            .withNetworkAliases("tbot")
            .dependsOn(tbotconf, redis, kafka, telegramDb)
            .withEnv("BOT_PROFILE", "test");


    private static String getPrivateKey() {
        try {
            return FileUtils.readFileToString(new File(System.getenv("HOMEPATH") + "/.ssh/id_rsa"), "UTF-8");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
