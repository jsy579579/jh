package com.jh.paymentgateway.util.xk;

import java.security.MessageDigest;


/**
 * MD5签名验签工具类
 * @author huangqiang
 *
 */
public class MD5Utils {

    /**
     * MD5签名
     * 
     * @param paramSrc
     *		签名内容
     * @param key
     *   	签名key
     * @return
     * @throws Exception
     */
    public static String sign(String paramSrc,String key) {
        String sign = md5(paramSrc + "&key=" + key);
        return sign;
    }

    /**
     * MD5验签
     * 
     * @param source
     *    	签名内容
     * @param sign
     *   	签名值
     * @param key
     *   	签名key
     * @return
     */
    public static boolean verify(String source, String sign,String key) {
        String signature = md5(source + "&key=" + key);
        return sign.equals(signature);
    }

    final static String md5(String paramSrc) {
        char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
        try {
            byte[] btInput = paramSrc.getBytes("UTF-8");
            // 获得MD5摘要算法的 MessageDigest 对象
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            // 使用指定的字节更新摘要
            mdInst.update(btInput);
            // 获得密文
            byte[] md = mdInst.digest();
            // 把密文转换成十六进制的字符串形式
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str).toLowerCase();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
