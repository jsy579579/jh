package com.jh.channel.util;


import java.security.MessageDigest;


public class MD5Util {

    public static String getSignature(String dataStr, String key) throws Exception {
        return encryptMD5(dataStr, key, "UTF-8"); // MD5加密得到签名字符串
    }


    public static String encryptMD5(String dataStr, String key, String encoded) throws Exception {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        md5.update(dataStr.getBytes(encoded));
        StringBuffer result = new StringBuffer();
        byte[] temp = md5.digest(key.getBytes(encoded));
        for (int i = 0; i < temp.length; i++) {
            result.append(Integer.toHexString((0x000000ff & temp[i]) | 0xffffff00).substring(6));
        }
        return result.toString();
    }

}
