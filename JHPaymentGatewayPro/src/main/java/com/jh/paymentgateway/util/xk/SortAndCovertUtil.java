package com.jh.paymentgateway.util.xk;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;


/**
 * 	字典序排序工具类
 * @author huangqiang
 *
 */
public class SortAndCovertUtil {
	
	private static Logger logger = LoggerFactory.getLogger(SignMessageUtil.class);
	
	/**
	 * 	将map数据按key=value&key=value...形式字符串输出
	 * @param map
	 * @return
	 */
	public static String covertToStr(Map<String, String> map) {
		TreeMap<String, String> treeMap = new TreeMap<String, String>();
		treeMap.putAll(map);
		Iterator<Entry<String, String>> iterator = treeMap.entrySet().iterator();
		StringBuffer sb = new StringBuffer();
		while(iterator.hasNext()) {
			Entry<String, String> entry = iterator.next();
			String key = entry.getKey();
			String value = entry.getValue();
			if(StringUtils.isNotBlank(value)) {
				sb.append(key).append("=").append(value).append("&");
			}
		}
		if(sb.length() == 0) {
			logger.info("未输入参数或所有输入参数均无值");
			return null;
		}
		String res = sb.substring(0, sb.length()-1);
		return res;
	}
	
}
