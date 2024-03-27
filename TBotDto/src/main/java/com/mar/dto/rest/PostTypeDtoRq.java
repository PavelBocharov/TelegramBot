package com.mar.dto.rest;

import lombok.*;

import java.util.List;

@With
@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class PostTypeDtoRq extends BaseRq {

    private Long id;
    private String title;
    private List<String> lines;

    public PostTypeDtoRq(String title, List<String> lines) {
        this.title = title;
        this.lines = lines;
    }

    public PostTypeDtoRq(long id) {
        this.id = id;
    }

}
