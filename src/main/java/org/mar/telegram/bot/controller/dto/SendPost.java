package org.mar.telegram.bot.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class SendPost extends BaseRq {

    @NotNull
    private Long userId;
    @NotBlank
    private String filePath;
    private Map<String, String> caption;
    private List<String> hashTags;

}
