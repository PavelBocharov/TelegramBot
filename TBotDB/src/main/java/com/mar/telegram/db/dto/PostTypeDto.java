package com.mar.telegram.db.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;

@With
@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class PostTypeDto extends BaseDto {

    private Long id;
    @NotBlank
    private String title;
    @NotEmpty
    private List<String> lines;

}
