package org.mar.telegram.bot.service.bot;

import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Message;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

@Data
@With
@NoArgsConstructor
@AllArgsConstructor
class MessageStatus {

    private Message msg;
    private CallbackQuery callbackQuery;
    private Long msgUserId;
    private Boolean isSuccess = false;

}