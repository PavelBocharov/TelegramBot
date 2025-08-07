package com.mar.dto.tbot;

import com.mar.dto.rest.BaseRq;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

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
