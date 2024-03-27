package com.mar.dto.mq;

import com.mar.dto.tbot.ContentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class LoadFileInfo implements Serializable {
    private String fileUrl;
    private String saveToPath;
    private String typeDir;
    private String fileName;
    private Long chatId;
    private String fileType;
    private ContentType mediaType;
    private String textFromUi;
}
