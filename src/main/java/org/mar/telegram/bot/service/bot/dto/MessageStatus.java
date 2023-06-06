package org.mar.telegram.bot.service.bot.dto;

import com.pengrad.telegrambot.model.Message;
import lombok.*;

import java.io.Serializable;

@Data
@With
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class MessageStatus implements Serializable {

    private String rqUuid;
    private Message msg;
    private CallbackQueryDto query;
    private Long msgUserId;
    private Boolean isSuccess = false;

}