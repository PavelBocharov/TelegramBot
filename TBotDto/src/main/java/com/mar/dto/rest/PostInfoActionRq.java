package com.mar.dto.rest;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class PostInfoActionRq extends BaseRq {

    @NotNull
    private Long adminId;
    private String likeCaption;
    @Min(0)
    private Long page;
    @Min(1)
    private Long size;
    @NotNull
    private OrderColumn orderColumn;
    @NotNull
    private OrderType orderType;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public enum OrderColumn {

        ID("id", "Default", OrderType.DESC),
        RATE_DESC("ord", "Rate ↑", OrderType.DESC),
        RATE_ASC("ord", "Rate ↓", OrderType.ASC),
        ADMIN_DESC("admin_action", "Super user ↑", OrderType.DESC),
        ADMIN_ASC("admin_action", "Super user ↓", OrderType.ASC);

        private String tableName;
        private String title;
        private OrderType orderType;

    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public enum OrderType {
        DESC("desc nulls last"),
        ASC("asc nulls first");

        private String sqlQuery;

    }

}
