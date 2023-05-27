package org.mar.telegram.bot.service.bot;

import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import jakarta.annotation.PostConstruct;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.boot.logging.LogLevel;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class TelegramBotController extends TelegramBotUtils {

    @PostConstruct
    public void postInit() {
        bot.setUpdatesListener(updates -> {
            worker(updates);
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }

    private void worker(List<Update> updates) {
        AtomicReference<String> rqUuid = new AtomicReference<>();
        Flux.fromIterable(updates)
                .map(this::createMessageStatus)
                .map(messageStatus -> {
                    rqUuid.set(messageStatus.getRqUuid());
                    return messageStatus;
                })
                .map(this::checkCallbackQuery)
                .map(this::checkAdmin)
                .map(this::parsText)
                .map(this::savePhoto)
                .map(this::saveVideo)
                .map(this::saveAnimation)
                .map(this::saveDocs)
                .doOnError(throwable ->
                        mqSender.sendLog(rqUuid.get(), LogLevel.ERROR, ExceptionUtils.getRootCauseMessage(throwable))
                )
                .subscribe(messageStatus -> userInfoService.getByUserId(rqUuid.get(), messageStatus.getMsgUserId()));
    }


}
