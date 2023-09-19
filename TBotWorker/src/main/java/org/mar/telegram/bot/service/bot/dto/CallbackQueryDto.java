package org.mar.telegram.bot.service.bot.dto;

import lombok.*;

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
