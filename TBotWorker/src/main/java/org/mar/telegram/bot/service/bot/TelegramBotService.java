package org.mar.telegram.bot.service.bot;

import org.mar.telegram.bot.controller.dto.TelegramMessage;
import org.mar.telegram.bot.service.bot.dto.MessageStatus;
import org.springframework.stereotype.Service;

@Service
public class TelegramBotService extends TelegramBotUtils {

    public void workWithMessage(TelegramMessage msg) {
        MessageStatus status = createMessageStatus(msg);
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
