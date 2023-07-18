package org.mar.telegram.bot.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class SendPost implements Serializable {

    @NotBlank
    private String rqUuid;
    @NotNull
    private Date rqTm;
    @NotNull
    private Long userId;
    @NotBlank
    private String filePath;
    private Map<String, String> caption;
    private List<String> hashTags;

}
