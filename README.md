# <img src="./src/main/resources/img/icon/bot.png" width="32"/> TelegramBot
Small project for learn etc. and actual skills.

<img src="./src/main/resources/img/telegram.jpg" width="" height="512"/>

## <img src="./src/main/resources/img/icon/roadmap.png" width="24"/> Project stack
- Java 17
- Spring Boot
- Artemis MQ
- Docker/Docker compose
- Kubernetes
- Maven

## <img src="./src/main/resources/img/icon/arrow-right.png" width="24"/> Start application
### <img src="./src/main/resources/img/icon/spring.png" width="16"/> Spring Boot application
1) Set environment in [application.properties](./src/main/resources/application.properties):
   - `application.bot.token` - Telegram Bot token
   - `application.bot.directory.path` - local directory for download files
2) Build and start project
   - IDEA - start `Main.main()`
   - Maven 
     1) Compile jar - `mvn clean install`
     2) Start app - `java -jar ./target/TelegramBot*.jar`
### <img src="./src/main/resources/img/icon/docker-icon.png" width="16"/> Docker compose
1) Set environment in [.env](./.env)
    - `LOCAL_PC_MOUNT_DIR` - local directory for download files 
    - `TELEGRAM_BOT_TOKEN` - Telegram Bot token
2) Start - `docker compose up`
3) Stop - `docker compose down`
### <img src="./src/main/resources/img/icon/kubernetes.png" width="16"/> Kubernetes
0) Install Kubernetes - **[LINK](https://kubernetes.io/ru/docs/setup/learning-environment/minikube/)**
1) Build docker image - `docker build -t marolok/telegram_bot:1.0.0 .`
1) Push docker image - `docker push marolok/telegram_bot:1.0.0`
2) Set environment in [kube_pod.yaml](./k8s/kube_pod.yaml)
2) Start - `kubectl apply -f .\k8s\kube_pod.yaml`
3) Stop - `kubectl delete pod telegram-bot`

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