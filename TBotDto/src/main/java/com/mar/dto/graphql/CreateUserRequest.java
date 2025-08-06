package com.mar.dto.graphql;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mar.dto.rest.BaseRq;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequest extends BaseRq implements Serializable {

    @JsonIgnore
    private final String graph_ql = """
            mutation createUser {
                createUser(rqUuid: "%s", userId: "%d") {
                    rqUuid, rqTm, errorCode, errorMsg,
                    id, userId, actionIds
                }
            }
            """;
    private String query;
    private String operationName = GraphQLResponse.CREATE_USER_METHOD;

    public CreateUserRequest(String rqUuid, Date rqTm) {
        super(rqUuid, rqTm);
    }

    public CreateUserRequest setVariablesData(@NotNull String rqUuid, @NotNull Long userId) {
        query = graph_ql.formatted(rqUuid, userId);
        return this;
    }
}
