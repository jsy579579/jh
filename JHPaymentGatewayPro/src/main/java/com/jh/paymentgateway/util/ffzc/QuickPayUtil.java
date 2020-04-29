package com.jh.paymentgateway.util.ffzc;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Map;

public class QuickPayUtil {

	
	public static final String LINK_SIGN = "#";

	/**
	 * #连接符的形式
	 * @param reqMap
	 * @param signKey
	 * @return
	 */
	public static String generSign(Map<String, String> reqMap, String signKey) {
		StringBuilder sb = new StringBuilder().append(LINK_SIGN);
		for (Map.Entry<String, String> entry : reqMap.entrySet()) {
			sb.append(entry.getValue()).append(LINK_SIGN);
		}
		sb.append(signKey);
		String signBody = sb.toString();
		System.out.println(("签名体:" + signBody));
		return Md5Util.MD5(signBody);
	}

	/**
	 * 产生带前缀唯一订单
	 * 
	 * @return
	 */
	public static String createOrderNum(String prefix) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		String dateStr1 = sdf.format(new Date(0));
		return prefix + dateStr1 + RandomUtils.getRandomString(4);
	}

}
