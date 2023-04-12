# TelegramBot
Small project for learn etc. and actual skills.

<img src="./src/main/resources/img/telegram.jpg" width="" height="512"/>

## Project stack
- Java 17
- Spring Boot
- Artemis MQ
- Docker/Docker compose
- Maven

## Start application
### Spring Boot application
1) Set environment in [application.properties](./src/main/resources/application.properties):
   - `application.bot.token` - Telegram Bot token
   - `application.bot.directory.path` - local directory for download files
2) Build and start project
   - IDEA - start `Main.main()`
   - Maven 
     1) Compile jar - `mvn clean install`
     2) Start app - `java -jar ./target/TelegramBot*.jar`
### Docker compose
1) Set environment in [.end](./.env)
    - `LOCAL_PC_MOUNT_DIR` - local directory for download files 
    - `TELEGRAM_BOT_TOKEN` - Telegram Bot token
2) Start - `docker compose up`
3) Stop - `docker compose down`

## How to use
- Start chat with your Bot
- `Send text` - set file name for next file
- `Send file` - directory select by type file
  - _photos_ - jpg, jpeg, png, bmp
  - _videos_ - mp4
  - _gif_ - gif
  - _document_ -  non compress file
- `Send URL` - directory select by detected type
  - By MIME type
  - If full path has type (_http://test.org/test.png_ -> photos)
    - `.gifv` by [imgur.com](https://imgur.com/) converted to `.mp4`

## Screens
### Send text and image
<img alt="" src="./src/main/resources/img/screen_1.png" width="512"/>

### Send URL with MIME type
<img alt="" src="./src/main/resources/img/screen_2.png" width="512"/>