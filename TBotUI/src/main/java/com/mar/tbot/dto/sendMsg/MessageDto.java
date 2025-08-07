package com.mar.tbot.dto.sendMsg;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class MessageDto implements Serializable {

    private Long fromUserId;
    private String text;
    private Long chatId;
    private List<PhotoSizeDto> photoSizeList;
    private String documentFileId;
    private String videoFileId;
    private String animationFileId;
}
