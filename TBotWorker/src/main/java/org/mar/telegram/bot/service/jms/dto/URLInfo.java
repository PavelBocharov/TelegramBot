package org.mar.telegram.bot.service.jms.dto;

import lombok.*;
import org.mar.telegram.bot.utils.ContentType;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class URLInfo {

    private ContentType contentType;
    private String url;
    private String fileType;

}
