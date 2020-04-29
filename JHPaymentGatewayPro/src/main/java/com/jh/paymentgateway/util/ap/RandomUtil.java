package com.jh.paymentgateway.util.ap;

import java.util.Date;
import java.util.Random;

/**
 * Created by niejing on 2015/8/22.
 */
public class RandomUtil {

    /**
     * 生成指定长度的字符随机串
     *
     * @return String 生成的随机数字 其实r.nextInt(maxInt)就可以得到一个随机的小于maxInt的整数，没有必要再用一个字符数组str 。但是如果生成一个随机字符串，可以这样使用。
     */
    public static String generateCode(int codeLength) {
        // 44个字符
        final int maxNum = 44;
        int i; // 生成的随机数
        int count = 0; // 生成的密码的长度
        char[] str =
                { 'a', 'b', 'd', 'e', 'f', 'h', 'j', 'm', 'n', 'q', 'r', 't', 'u', 'v', 'w', 'x',
                        'g', 'y', 'z', 'A', 'B', 'D', 'E', 'F', 'G', 'H', 'J', 'K', 'L', 'M', 'N',
                        'T', 'Y', 'Q', 'R', '0', '2', '3', '4', '5', '6', '7', '8', '9' };

        StringBuffer pwd = new StringBuffer("");
        Random r = new Random();
        while (count < codeLength) {
            // 生成随机数，取绝对值，防止生成负数
            i = Math.abs(r.nextInt(maxNum)); // 生成的数最大为36-1

            if (i >= 0 && i < str.length) {
                pwd.append(str[i]);
                count++;
            }
        }

        return pwd.toString();
    }

    /**
     * 生成指定长度的纯数字随机串
     */
    public static String generateNumber(int codeLength) {
        // 10个数字
        final int maxNum = 8;
        int i; // 生成的随机数
        int count = 0; // 生成的密码的长度
        char[] str = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' };

        StringBuffer pwd = new StringBuffer("");
        Random r = new Random();
        while (count < codeLength) {
            // 生成随机数，取绝对值，防止生成负数
            i = Math.abs(r.nextInt(maxNum)); // 生成的数最大为36-1

            if (i >= 0 && i < str.length) {
                pwd.append(str[i]);
                count++;
            }
        }

        return pwd.toString();
    }

    /**
     * 获取订单号
     * @param pre 前缀
     * @return
     * @Description:
     */
    public static String generateNumber(String pre, String format) {
        StringBuilder sbTradeNo = new StringBuilder();

        sbTradeNo.append(pre);
        sbTradeNo.append(DateUtil.date2String(new Date(), format)).append(
                RandomUtil.generateNumber(2));
        return sbTradeNo.toString();
    }

    /**
     * 获取一个客户编号
     * @param 前缀
     * @return
     * @Description:
     */
    public static String generateCustomerNo(String pre) {
        StringBuilder sbTradeNo = new StringBuilder();
        sbTradeNo.append(pre);
        sbTradeNo.append(DateUtil.date2String(new Date(), DateUtil.YYMMDDHHMM)).append(
                RandomUtil.generateNumber(4));
        return sbTradeNo.toString();
    }
}
