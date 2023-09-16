package org.mar.telegram.bot.service.db.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum ActionEnum {

    FIRE_HEART("❤️\u200D\uD83D\uDD25", "fire"),
    DEVIL("\uD83D\uDE08", "devil"),
    COOL("\uD83D\uDE31", "0_0"),
    BORING("\uD83D\uDE15", "0-0");

    private String code;
    private String callbackData;

    public static ActionEnum getActionByCallbackData(Object callbackData) {
        return Arrays.stream(ActionEnum.values())
                .filter(actionEnum -> actionEnum.getCallbackData().equals(callbackData))
                .findFirst()
                .get();
    }

}
