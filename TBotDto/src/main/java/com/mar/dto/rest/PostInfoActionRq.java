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
    private Integer page;
    @Min(1)
    private Integer size;
    @NotNull
    private OrderColumn orderColumn;
    @NotNull
    private OrderType orderType;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public enum OrderColumn {

        ID("id"),
        ACTION_0("action0"),
        ACTION_1("action1"),
        ACTION_2("action2"),
        ACTION_3("action3"),
        CAPTION("caption");

        private String tableName;

    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public enum OrderType {
        DESC("desc"),
        ASC("asc");

        private String tableName;

    }

}
