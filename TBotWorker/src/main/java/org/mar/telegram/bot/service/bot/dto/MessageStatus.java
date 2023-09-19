package org.mar.telegram.bot.service.bot.dto;

import lombok.*;
import org.mar.telegram.bot.controller.dto.MessageDto;

import java.io.Serializable;

@Data
@With
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class MessageStatus implements Serializable {

    private String rqUuid;
    private MessageDto msg;
    private CallbackQueryDto query;
    private Long msgUserId;
    private Boolean isSuccess = false;

}