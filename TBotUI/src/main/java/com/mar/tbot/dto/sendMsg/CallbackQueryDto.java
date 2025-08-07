package com.mar.tbot.dto.sendMsg;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CallbackQueryDto implements Serializable {

    private Long fromUserId;
    private Long msgChatId;
    private Integer messageId;
    private String messageCaption;
    private String actionCallbackData;
    private String videoFieldId;
    private String animationFieldId;
    private String photoFieldId;
    private String documentFieldId;

}
