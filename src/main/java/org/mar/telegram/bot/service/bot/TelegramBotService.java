package org.mar.telegram.bot.service.bot;

import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import jakarta.annotation.PostConstruct;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.mar.telegram.bot.service.bot.dto.MessageStatus;
import org.springframework.boot.logging.LogLevel;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class TelegramBotService extends TelegramBotUtils {

    @PostConstruct
    public void postInit() {
        bot.setUpdatesListener(updates -> {
            worker(updates);
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }

    private void worker(List<Update> updates) {String rqUuid = UUID.randomUUID().toString();
        for (Update update : updates) {
            try {
                workWithMessage(rqUuid, update);
            } catch (Exception ex) {
                mqSender.sendLog(rqUuid, LogLevel.ERROR, ExceptionUtils.getStackTrace(ex));
            }
        }
    }

    public void workWithMessage(String rqUuid, Update msg) {
        MessageStatus status = createMessageStatus(rqUuid, msg);
        checkUser(status);
        checkCallbackQuery(status);
        checkAdmin(status);
        parsText(status);
        savePhoto(status);
        saveVideo(status);
        saveAnimation(status);
        saveDocs(status);
        checkEndStatus(status);
    }

}
