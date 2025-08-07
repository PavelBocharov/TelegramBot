package com.mar.tbot.dto.sendMsg;

import com.mar.dto.rest.BaseRq;
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
public class TelegramMessageRq extends BaseRq {

    private MessageDto msg;
    private CallbackQueryDto callbackQuery;

}
