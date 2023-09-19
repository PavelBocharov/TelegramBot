package org.mar.bot.integration.dto;

import lombok.*;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class SendPostDto implements Serializable {

    protected String rqUuid;
    protected Date rqTm;
    private Long userId;
    private String filePath;
    private Map<String, String> caption;
    private List<String> hashTags;

}
