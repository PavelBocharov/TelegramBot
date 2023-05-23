# <img src="./src/main/resources/img/icon/bot.png" width="32"/> TelegramBot
Small project for learn etc. and actual skills.

<img src="./src/main/resources/img/telegram.jpg" width="" height="512" alt="Art by Olena Yemelianova"/></br><sup><sub>Art by Olena Yemelianova</sub></sup>


## Links
* [Docker HUB](https://hub.docker.com/repository/docker/marolok/telegram_bot/general)
* [GitHub](https://github.com/PavelBocharov/TelegramBot)

## <img src="./src/main/resources/img/icon/roadmap.png" width="24"/> Project stack
- Language - Java 17
- Framework - Spring Boot
- Build - [Apache Maven](https://maven.apache.org/)
- MQ
  - Local - [Apache Artemis MQ (Active MQ)](https://activemq.apache.org/components/artemis/)
  - Docker - [Apache Kafka](https://kafka.apache.org/)
- Image
  - Docker/Docker compose
  - Kubernetes
    - Classic [.yaml](https://kubernetes.io/docs/home/)
    - Maven - [Eclipse JKube](https://www.eclipse.org/jkube/)
- Cache
  - Local - [Ehcache](https://www.ehcache.org/)
  - Docker - [Redis](https://redis.io/)
- DataBase
  - [TelegramDB](https://github.com/PavelBocharov/TelegramDB) (WebFlux + PostgreSQL)

## <img src="./src/main/resources/img/icon/arrow-right.png" width="24"/> Start application
### <img src="./src/main/resources/img/icon/spring.png" width="16"/> Spring Boot application - use Docker start
1) ~~Start [TelegramDB](https://github.com/PavelBocharov/TelegramDB)~~
2) ~~Set environment in [application.properties](./src/main/resources/application.yml):~~
   - ~~`application.bot.token` - Telegram Bot token~~
   - ~~`application.bot.directory.path` - local directory for download files~~
   - ~~`application.group.chat.id` - chat for post~~
   - ~~`application.bot.db.url` - TelegramDB URL~~
2) ~~Build and start project~~
   - ~~IDEA - start `Main.main()`~~
   - ~~Maven~~
     1) ~~Compile jar - `mvn clean install`~~
     2) ~~Start app - `java -jar ./target/TelegramBot*.jar`~~

### <img src="./src/main/resources/img/icon/docker-icon.png" width="16"/> Docker compose
0) [Start info](https://www.baeldung.com/ops/docker-compose)
1) Build JAR - `mvn clean install`
2) Set environment in [.env](./.env)
    - `LOCAL_PC_MOUNT_DIR` - local directory for download files
3) Set ENV in [docker-compose](./docker-compose.yml)
   - `tbotconf.GIT_URL` - your Git config (more [Spring Cloud Config](https://docs.spring.io/spring-cloud-config/docs/current/reference/html/))
   - `tbotconf.PRIVATE_KEY` - use SSH private key for connect (example [GitHub](https://docs.github.com/en/authentication/connecting-to-github-with-ssh/adding-a-new-ssh-key-to-your-github-account))
4) Set config in your Git repo ([TelegramConf](https://github.com/PavelBocharov/TelegramConf/blob/main/telegram-bot-image.yml))
5) Start - `docker compose up`
4) Stop - `docker compose down`

### <img src="./src/main/resources/img/icon/kubernetes.png" width="16"/> Kubernetes
* Install Kubernetes(Minikube) - **[LINK](https://kubernetes.io/ru/docs/setup/learning-environment/minikube/)**
* Start Minikube
  * `minikube start`
  * `minikube mount <source directory>:<target directory>` - [Mounting filesystems](https://minikube.sigs.k8s.io/docs/handbook/mount/)
    * Example: `minikube mount D:/temp:/local/` - `<target directory>` equals `Pod.spec.volumes.hostPath` in [kube_pod.yaml](./k8s/kube_pod.yaml) or `MOUNT_VM_PATH` in [pom.xml](./pom.xml)

#### <img src="./src/main/resources/img/icon/yaml.png" width="16"/> Start with YAML
* Build docker image - `docker build -t marolok/telegram_bot:1.0.0 .`
* Push docker image - `docker push marolok/telegram_bot:1.0.0`
* Set environment in [kube_pod.yaml](./k8s/kube_pod.yaml) - use mount target directory from `Start Minikube`
* Init config - `kubectl apply -f .\k8s\kube_config.yaml`
* Start - `kubectl apply -f .\k8s\kube_pod.yaml`
* Stop - `kubectl delete pod telegram-bot`

#### <img src="./src/main/resources/img/icon/jkube.png" width="20"/> 🚧 WIP 🏗️ Start with [JKube Maven Plugin](https://www.eclipse.org/jkube/) ⚠️NOT STABLE⚠️
* Set environment in [pom.xml](./pom.xml) - use mount target directory from `Start Minikube`
* Package application - `mvn clean package`
* Build Docker image - `mvn k8s:build`
* Create Kubernetes config file - `mvn k8s:resource`
* Start - `mvn k8s:apply`
  * You can use one command - `mvn clean package k8s:build k8s:resource k8s:apply`

## <img src="./src/main/resources/img/icon/direction.png" width="24"/> How to use
- Start chat with your Bot
- `Send text` - set file name for next file - [example](#-send-text-and-image)
- `Send file` - directory select by type file - [example](#-send-text-and-image)
  - _photos_ - jpg, jpeg, png, bmp
  - _videos_ - mp4
  - _gif_ - gif
  - _document_ -  non compress file
- `Send URL` - directory select by detected type
  - By MIME type - [example](#-send-url-with-mime-type)
  - If full path has type (_http://test.org/test.png_ -> photos)
    - `.gifv` by [imgur.com](https://imgur.com/) converted to `.mp4`

## <img src="./src/main/resources/img/icon/image.png" width="24"/> Screens
#### ⚬ Send text and image
<img alt="" src="./src/main/resources/img/screen_1.png" width="512"/>

#### ⚬ Send URL with MIME type
<img alt="" src="./src/main/resources/img/screen_2.png" width="512"/>
