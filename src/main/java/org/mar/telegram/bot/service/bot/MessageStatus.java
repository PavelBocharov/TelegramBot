package org.mar.telegram.bot.service.bot;

import com.pengrad.telegrambot.model.Message;
import lombok.*;
import org.mar.telegram.bot.service.bot.dto.CallbackQueryDto;

import java.io.Serializable;

@Data
@With
@ToString
@NoArgsConstructor
@AllArgsConstructor
class MessageStatus implements Serializable {

    private String rqUuid;
    private Message msg;
    private CallbackQueryDto query;
    private Long msgUserId;
    private Boolean isSuccess = false;

}