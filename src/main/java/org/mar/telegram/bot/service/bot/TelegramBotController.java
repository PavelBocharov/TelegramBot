package org.mar.telegram.bot.service.bot;

import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.mar.telegram.bot.service.bot.db.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;

@Slf4j
@Service
public class TelegramBotController extends TelegramBotUtils {

    @Autowired
    private UserService userInfoService;

    @PostConstruct
    public void postInit() {
        bot.setUpdatesListener(updates -> {
            worker(updates);
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }

    private void worker(List<Update> updates) {
        Flux.fromIterable(updates)
                .map(this::createMessageStatus)
                .map(this::checkCallbackQuery)
                .map(this::checkAdmin)
                .map(this::parsText)
                .map(this::savePhoto)
                .map(this::saveVideo)
                .map(this::saveAnimation)
                .map(this::saveDocs)
                .doOnError(throwable -> log.error(ExceptionUtils.getRootCauseMessage(throwable)))
                .subscribe(messageStatus -> userInfoService.getByUserId(messageStatus.getMsgUserId()));
    }


}
