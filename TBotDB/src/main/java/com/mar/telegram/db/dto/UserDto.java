package com.mar.telegram.db.dto;

import lombok.*;

import java.util.List;

@With
@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class UserDto extends BaseDto {

    protected Long id;
    protected Long userId;
    protected List<Long> actionIds;

}
