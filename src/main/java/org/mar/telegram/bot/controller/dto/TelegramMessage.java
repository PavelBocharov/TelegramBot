package org.mar.telegram.bot.controller.dto;

import jakarta.validation.Valid;
import lombok.*;
import org.mar.telegram.bot.service.bot.dto.CallbackQueryDto;

@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class TelegramMessage extends BaseRq {

    @Valid
    private MessageDto msg;
    @Valid
    private CallbackQueryDto callbackQuery;

}
