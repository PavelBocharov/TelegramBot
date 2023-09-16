package com.mar.telegram.db.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class ActionPostDto implements Serializable {

    private Long id;
    private Long postId;
    private Long userId;
    private String actionCallbackData;

}
