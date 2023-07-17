package org.mar.telegram.bot.controller.dto;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class SendPost implements Serializable {

    private String rqUuid;
    private LocalDateTime rqTm;
    private Long userId;
    private String filePath;
    private Map<String, String> caption;
    private List<String> hashTags;

}
