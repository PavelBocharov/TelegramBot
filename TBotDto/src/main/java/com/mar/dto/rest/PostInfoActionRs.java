package com.mar.dto.rest;

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
public class PostInfoActionRs implements Serializable {

    private Long id;
    private String caption;
    private Boolean adminAction;
    private Long action0;
    private Long action1;
    private Long action2;
    private Long action3;

}
