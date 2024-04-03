package com.mar.telegram.db.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class PostInfoAction implements Serializable {

    private Long id;
    private String caption;
    @JsonProperty("admin_action")
    private Boolean adminAction;
    private Long action0;
    private Long action1;
    private Long action2;
    private Long action3;

}
