package com.mar.tbot.utils;

import lombok.experimental.UtilityClass;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@UtilityClass
public class Utils {

    public static Properties loadProperties(String resourceFileName) throws IOException {
        Properties configuration = new Properties();
        InputStream inputStream = Utils.class
                .getClassLoader()
                .getResourceAsStream(resourceFileName);
        configuration.load(inputStream);
        inputStream.close();
        return configuration;
    }

}
