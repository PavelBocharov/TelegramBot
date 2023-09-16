package com.mar.telegram.db.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;

@With
@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class HashTagListDto extends BaseDto {

    @NotEmpty
    private List<HashTagDto> tags;

}
