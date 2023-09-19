package org.mar.telegram.bot.service.db.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.mar.telegram.bot.controller.dto.BaseRs;

import java.io.Serializable;
import java.util.List;

@With
@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class UserDto extends BaseRs {

    private Long id;
    private Long userId;
    private List<Long> actionIds;

}
