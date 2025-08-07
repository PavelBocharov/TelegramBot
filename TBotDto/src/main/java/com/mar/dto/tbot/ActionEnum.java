package com.mar.dto.tbot;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum ActionEnum {

    FIRE_HEART("❤️\u200D\uD83D\uDD25", "fire"),
    DEVIL("\uD83D\uDE08", "devil"),
    BORING("\uD83D\uDE15", "0_0"),
    BAD("\uD83D\uDC4E", "0-0");

    private String code;
    private String callbackData;

    public static ActionEnum getActionByCallbackData(Object callbackData) {
        return Arrays.stream(ActionEnum.values())
                .filter(actionEnum -> actionEnum.getCallbackData().equals(callbackData))
                .findFirst()
                .get();
    }

}
