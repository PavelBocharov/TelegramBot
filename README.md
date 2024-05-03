# 🤖 TelegramBot
Small project for learn etc. and actual skills.

<img alt="Art by Olena Yemelianova" height="512" src="./TBotWorker/src/main/resources/img/telegram.jpg"/></br><sup><sub>Art by Olena Yemelianova</sub></sup>

## 💫 Release versions
* Telegram Bot (main): `marolok/telegram_bot:2.5.0`
* Telegram Bot UI: `marolok/telegram-bot-ui:1.8.0`
* Telegram Bot DB: `marolok/telegram_db:2.4.0`
* Telegram Bot Config: `marolok/telegram_conf:1.2.0`

## 🔗 Links
* [🚀 Start application ](./doc/StartApp.md)
* [💾 How to use...](./doc/HowWork.md)
* [GitHub](https://github.com/PavelBocharov/TelegramBot)
* [Docker HUB](https://hub.docker.com/repositories/marolok)

## 📚 Project stack
- Language - Java 17
- Framework - Spring Boot
- Build - [Apache Maven](https://maven.apache.org/)
- MQ
  - Local - [Apache Artemis MQ (Active MQ)](https://activemq.apache.org/components/artemis/)
  - Docker - [Apache Kafka](https://kafka.apache.org/)
- Image
  - Build - [Fabric8](https://dmp.fabric8.io/)
  - Docker/Docker compose
  - Kubernetes
- Cache
  - Local - [Ehcache](https://www.ehcache.org/)
  - Docker - [Redis](https://redis.io/)
- DataBase
  - Local
    - TelegramBot - use cache
    - [TelegramDB](https://github.com/PavelBocharov/TelegramDB) - [H2](https://www.h2database.com/html/main.html)
  - Docker - [TelegramDB](https://github.com/PavelBocharov/TelegramDB) (WebFlux + PostgreSQL)
- Configuration - [Spring Cloud Config](https://docs.spring.io/spring-cloud-config/docs/current/reference/html/). My image -> [DockerHub](https://hub.docker.com/repository/docker/marolok/telegram_conf/general)
- Test
  - Unit test - JUnit 5
  - Integration test - [Testcontainers](https://www.testcontainers.org/)
