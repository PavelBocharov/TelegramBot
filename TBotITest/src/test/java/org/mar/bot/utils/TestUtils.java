package org.mar.bot.utils;

import lombok.experimental.UtilityClass;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.isNumeric;

@UtilityClass
public class TestUtils {

    private static Properties properties;

    public static String getPropertyStr(String propName) {
        if (isNull(properties)) {
            try (InputStream input = new FileInputStream("src/test/resources/application-test.properties")) {
                properties = new Properties();
                properties.load(input);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        String rez = properties.getProperty(propName);

        if (isNotBlank(rez)) {
            return rez.trim();
        } else {
            throw new RuntimeException(String.format("Property by '%s' is blank.", propName));
        }
    }

    public static int getPropertyInt(String propName) {
        String prop = getPropertyStr(propName);
        if (isNumeric(prop)) {
            return Integer.parseInt(prop);
        } else {
            throw new RuntimeException(String.format("Property by '%s' is blank.", propName));
        }
    }

    public static long getPropertyLong(String propName) {
        String prop = getPropertyStr(propName);
        if (isNumeric(prop)) {
            return Long.parseLong(prop);
        } else {
            throw new RuntimeException(String.format("Property by '%s' is blank.", propName));
        }
    }

}
