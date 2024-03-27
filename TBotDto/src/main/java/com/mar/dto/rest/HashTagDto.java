package com.mar.dto.rest;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.With;

@With
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class HashTagDto {

    private Long id;
    private String tag;

}
