package com.mar.tbot.dto.sendMsg;

import com.mar.tbot.dto.BaseRq;
import lombok.*;

@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class TelegramMessage extends BaseRq {

    private MessageDto msg;
    private CallbackQueryDto callbackQuery;

}
