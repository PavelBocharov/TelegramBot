package com.mar.dto.graphql;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Map;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Error implements Serializable {

    private String message;
    private Location[] locations;
    private String[] path;
    private Map<String, Object> extensions;

}
