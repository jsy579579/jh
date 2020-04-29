package com.jh.paymentgateway.controller.yxe.util;

import java.security.MessageDigest;

/**
 * <b>功能说明:MD5工具类
 * </b>
 */
public class MD5Util {

    /**
     * 私有构造方法,将该工具类设为单例模式.
     */
    private MD5Util() {
    }

    private static final String[] hex = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f" };

    public static String encode(String password) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] byteArray = md5.digest(password.getBytes("utf-8"));
            String passwordMD5 = byteArrayToHexString(byteArray);
            return passwordMD5;
        } catch (Exception e) {
            e.fillInStackTrace();
        }
        return password;
    }

    public static String encode(String password , String enc) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] byteArray = md5.digest(password.getBytes(enc));
            String passwordMD5 = byteArrayToHexString(byteArray);
            return passwordMD5;
        } catch (Exception e) {
            e.fillInStackTrace();
        }
        return password;
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
    
    public static void main(String[] args) {
    	String aaa=encode("notifyUrl=http://tranpay.wangliany.com/tranpay/third/qy/receive&orderIp=192.168.1.1&orderPrice=1&orderTime=20180803162400&outTradeNo=100000000000000000001&payKey=7e1bb7f274784e0dbd34ee8a5759f18e&productName=测试&productType=40000103&remark=remark&returnUrl=http://www.baidu.com&secretContent=Lb3jM/DYGKCSNvn6Q71Yt+VNaJgknUUP92/HG0Gr6bZWCBODBLhGjipQgngDUQPdYQ0LXm4AmWn7"
    			+"7IebB16MAb0vSjOqiKazgNga9ehgTvabwhRK/qcOcRi8nk5onjDyNAekKp7EAhFFYgHcgTJb7S9b"
    			+"tKF0VLwSMz+exZSxnnGgOy+8b0ZyI0i2AODE1tP02YB4ZOUDN+cCyWpGkqhE7JxZycIU0JQbp43n"
    			+"eGNnqEFzlArboEyKCRLUlv8mHQ5sOmnetlHxZ/jRSuN5gKWMIA==&paySecret=1ab64b9f2fb408c93d1bbdc48524916").toUpperCase();
    	System.out.println(aaa);
	}
}
