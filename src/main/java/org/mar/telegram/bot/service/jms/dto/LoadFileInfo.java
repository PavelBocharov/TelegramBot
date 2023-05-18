package org.mar.telegram.bot.service.jms.dto;

import lombok.*;
import org.mar.telegram.bot.utils.ContentType;

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
}
