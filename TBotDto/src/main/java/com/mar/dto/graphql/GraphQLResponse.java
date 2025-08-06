package com.mar.dto.graphql;

import com.mar.dto.rest.BaseRs;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Map;

//    {
//        "errors": [
//            {
//                "message": "BaseException: NullPointerException: The mapper [com.mar.telegram.db.resolver.UserResolver$$Lambda$2241/0x000001b0dfba1580] returned a null value.",
//                    "locations": [
//                        {
//                            "line": 1,
//                                "column": 17
//                        }
//                    ],
//                "path": [
//                    "getUser"
//                ],
//                "extensions": {
//                "rqUuid": "b30e6697-1133-4715-b6b5-c4cd8581e7b0",
//                        "errorMsg": "NullPointerException: The mapper [com.mar.telegram.db.resolver.UserResolver$$Lambda$2241/0x000001b0dfba1580] returned a null value.",
//                        "rqTm": "Sun Jul 13 18:22:19 MSK 2025",
//                        "errorCode": "500",
//                        "classification": "INTERNAL_ERROR"
//                }
//            }
//        ],
//        "data": {
//            "getUser": null
//        }
//    }
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class GraphQLResponse extends BaseRs implements Serializable {

    public static String GET_USER_METHOD = "getUser";
    public static String CREATE_USER_METHOD = "createUser";

    private Error[] errors;
    private Map<String, Object> data;

}
