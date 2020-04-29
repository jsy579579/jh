package com.jh.paymentgateway.util.hxdh;

import java.security.MessageDigest;

/**
 * @author Administrator
 * @title: MD5Utils
 * @projectName quanyishop
 * @description: TODO
 * @date 2019/7/25 002511:11
 */
public class MD5Utils {

    /**
     * 私有构造方法,将该工具类设为单例模式.
     */
    private MD5Utils() {
    }

    private static final String[] hex = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f" };

    public static String encode(String param,String charsetName) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] byteArray = md5.digest(param.getBytes(charsetName));
            return byteArrayToHexString(byteArray);
        } catch (Exception e) {
            e.fillInStackTrace();
        }
        return param;
    }

    private static String byteArrayToHexString(byte[] byteArray) {
        StringBuffer sb = new StringBuffer();
        for (byte b : byteArray) {
            sb.append(byteToHexChar(b));
        }
        return sb.toString();
    }

    private static Object byteToHexChar(byte b) {
        int n = b;
        if (n < 0) {
            n = 256 + n;
        }
        int d1 = n / 16;
        int d2 = n % 16;
        return hex[d1] + hex[d2];
    }
}



