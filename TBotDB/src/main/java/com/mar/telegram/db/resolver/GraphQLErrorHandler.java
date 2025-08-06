package com.mar.telegram.db.resolver;

import com.mar.exception.BaseException;
import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.stereotype.Component;

import java.util.Map;

import static java.lang.String.valueOf;

@Slf4j
@Component
public class GraphQLErrorHandler extends DataFetcherExceptionResolverAdapter {

    @Override
    protected GraphQLError resolveToSingleError(Throwable ex, DataFetchingEnvironment env) {
        log.error("GraphQLErrorHandler ex: {}", ex.getMessage(), ex);
        if (ex instanceof BaseException exception) {
            return GraphqlErrorBuilder.newError()
                    .errorType(ErrorType.INTERNAL_ERROR)
                    .message(valueOf(exception.getErrorMsg()))
                    .path(env.getExecutionStepInfo().getPath())
                    .location(env.getField().getSourceLocation())
                    .extensions(
                            Map.of(
                                    "rqUuid", valueOf(exception.getRqUuid()),
                                    "rqTm", valueOf(exception.getRqTm()),
                                    "errorCode", valueOf(exception.getErrorCode()),
                                    "errorMsg", valueOf(exception.getErrorMsg())
                            )
                    )
                    .build();
        }
        return super.resolveToSingleError(ex, env);
    }
}
