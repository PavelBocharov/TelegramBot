package org.mar.telegram.bot.service.bot.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CallbackQueryDto implements Serializable {

    @NotNull
    private Long fromUserId;
    @NotNull
    private Long msgChatId;
    @NotNull
    private Integer messageId;
    private String messageCaption;
    @NotBlank
    @Schema(description = "User action: fire, devil, 0_0, 0-0.", defaultValue = "fire")
    private String actionCallbackData;
    private String videoFieldId;
    private String animationFieldId;
    private String photoFieldId;
    private String documentFieldId;

}
