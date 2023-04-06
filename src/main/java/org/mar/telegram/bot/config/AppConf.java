package org.mar.telegram.bot.config;

import com.pengrad.telegrambot.TelegramBot;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class AppConf {

    public static final String TELEGRAM_BOT_MQ = "telegram_queue";

    @Value("${application.bot.token}")
    private String botToken;

    @Bean
    @Scope("singleton")
    public TelegramBot telegramBot() {
        OkHttpClient client = new OkHttpClient();
        return new TelegramBot
                .Builder(botToken)
                .okHttpClient(client)
                .build();
    }

}
