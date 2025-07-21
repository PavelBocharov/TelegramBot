package com.mar.dto.tbot;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.With;

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