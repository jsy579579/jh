package com.jh.paymentgateway.util.ap;

import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigUtils {

    private static String     propPath =  "config.properties";
    private static Properties prop     = null;

    static {
        prop = new Properties();
        ClassPathResource classPathResource = new ClassPathResource(propPath);
        try {
            InputStream in = classPathResource.getInputStream();
            if(in == null){
            	in = ClassLoader.getSystemResourceAsStream(File.separator+propPath);
            }
            prop.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getProperty(String keyName) {
        return prop.getProperty(keyName);
    }

    public static String getProperty(String keyName, String defaultValue) {
        return prop.getProperty(keyName, defaultValue);
    }

}
