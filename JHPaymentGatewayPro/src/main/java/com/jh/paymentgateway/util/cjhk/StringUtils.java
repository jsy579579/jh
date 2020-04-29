package com.jh.paymentgateway.util.cjhk;

/**
 * Description:
 *
 * @author yingjie.wang
 * @since 17/4/14 上午11:08
 */
public class StringUtils {

    public static boolean isNotBlank(CharSequence cs) {
        return !isBlank(cs);
    }

    public static boolean isBlank(CharSequence cs) {
        int strLen;
        if (cs == null || (strLen = cs.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if ((Character.isWhitespace(cs.charAt(i)) == false)) {
                return false;
            }
        }
        return true;
    }

    public static String trim(String text) {
        return text == null ? "" : text.trim();
    }
}
