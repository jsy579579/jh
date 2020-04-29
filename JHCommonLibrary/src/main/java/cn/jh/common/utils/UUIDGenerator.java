package cn.jh.common.utils;

import java.util.*;

/**
 * UUID
 */
public class UUIDGenerator {


    /**
     * 生成uuid 例如（300c2977df2749adae55f3f2fc5f5a）
     * @return
     */
    public static String getUUID() {
        String s = UUID.randomUUID().toString();
        return s.substring(0, 8) + s.substring(9, 13) + s.substring(14, 18) + s.substring(19, 23) + s.substring(26);
    }

	/**
	 * 根据时间生成字符串 例如（2019101116122279950）
	 * @return
	 */
	public static String getDateTimeOrderCode() {
        String nowDateTimeStr = DateUtil.getDateStringConvert(new String(), new Date(), "yyyyMMddHHmmssSSS");
        return nowDateTimeStr + new Random().nextInt(10) + new Random().nextInt(10);
    }


}
