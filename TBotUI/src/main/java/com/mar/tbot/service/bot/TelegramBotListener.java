package com.mar.tbot.service.bot;

import com.mar.tbot.dto.sendMsg.TelegramMessageRq;
import com.mar.tbot.mapper.TelegramDataMapper;
import com.mar.tbot.service.ApiService;
import com.mar.utils.Utils;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import javax.annotation.PostConstruct;

import static java.util.Objects.nonNull;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramBotListener {

    private final TelegramBot bot;
    private final ApiService apiService;

    @Value("${application.bot.token}")
    private String botToken;

    @PostConstruct
    public void postInit() {
        bot.setUpdatesListener(updates -> {
            updates.forEach(this::worker);
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }

    private void worker(Update update) {
        try {
            if (nonNull(update)) {
                log.warn(">> msg - {}", update.message());
                TelegramMessageRq msg = new TelegramMessageRq();
                msg.setRqUuid(Utils.rqUuid());
                msg.setRqTm(new Date());
                msg.setMsg(TelegramDataMapper.toDto(update.message()));
                msg.setCallbackQuery(TelegramDataMapper.toDto(update.callbackQuery()));
                apiService.sendMsg(msg);
            }
        } catch (Exception ex) {
            log.error("TBot work exception: \n{}\n", ExceptionUtils.getStackTrace(ex));
        }
    }

}
