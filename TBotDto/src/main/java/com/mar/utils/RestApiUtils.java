package com.mar.utils;

import com.mar.dto.rest.BaseRs;
import lombok.experimental.UtilityClass;

import java.util.Date;

@UtilityClass
public class RestApiUtils {

    public static <T extends BaseRs> T enrichRs(T rs, String rqUuid) {
        if (rs != null) {
            rs.setRqUuid(rqUuid);
            rs.setRqTm(new Date());
        }
        return rs;
    }

}
