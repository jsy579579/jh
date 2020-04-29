package com.jh.paymentgateway.util.tyt;

import java.security.MessageDigest;
import java.util.Map;
import java.util.TreeMap;

public class Md5Util {

	/**
	 * @param str
	 * @return
	 * @throws Exception
	 */
	public static String MD5(String str) throws Exception {

		StringBuffer sb = new StringBuffer(32);
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] array = md.digest(str.getBytes("utf-8"));
			for (int i = 0; i < array.length; i++) {
				sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).toUpperCase().substring(1, 3));
			}
		} catch (Exception e) {
			System.out.println("字符串：" + str + "进行MD5失败");
			return null;
		}
		return sb.toString().toUpperCase();
	}

	private static <T> Map<String, Object> sortMapByKey(Map<String, T> map) {

		if (map == null || map.isEmpty()) {
			return null;
		}
		Map<String, Object> sortMap = new TreeMap<>(
		        (o1, o2) -> (o1 + "").compareTo(o2 + ""));
		sortMap.putAll(map);
		return sortMap;
	}
	
	/**
	 * 返回正确的待签名串
	 * 
	 * @param param
	 * @return
	 */
	public static <T> String getSignDataStr(Map<String, T> param) {

		Map<String, Object> map = sortMapByKey(param);
		String dataStr = "";
		for (java.util.Map.Entry<String, Object> e : map.entrySet()) {
			if (!e.getKey().equals("sign") && e.getValue() != null && e.getValue().toString().length() > 0) {
				if (dataStr.length() != 0) {
					dataStr += "&";
				}
				dataStr += e.getKey() + "=" + e.getValue();
			}
		}
		return dataStr;
	}
}
