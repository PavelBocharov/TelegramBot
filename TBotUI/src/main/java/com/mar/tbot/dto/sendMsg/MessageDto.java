package com.mar.tbot.dto.sendMsg;

import lombok.*;

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
