package com.jh.paymentgateway.controller.xs.util;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class MapUtils {
	/** 
     * 使用 Map按key进行排序 
     * @param map 
     * @return 
     */  
    public static Map<String, String> sortMapByKey(Map<String, String> map) {  
        if (map == null || map.isEmpty()) {  
            return null;  
        }  
        Map<String, String> sortMap = new TreeMap<String, String>(new Comparator<String>() {
        	public int compare(String str1, String str2) {  
    	        return str1.compareTo(str2);  
    	    }  
		});  
        sortMap.putAll(map);  
        return sortMap;
	}
	
    /**
     * 将map转换成url参数形式 a=1&b=1
     * @param map
     * @return
     */
    public static String mapToUrlParam(Map<String, Object> mapParam) {
    	if (mapParam == null || mapParam.isEmpty()) {  
            return "";  
        }
    	StringBuffer sb = new StringBuffer();
		int i = 0;
		for (String key : mapParam.keySet()) {
			i++;
			if (i > 1) {
				sb.append("&");
			}
			sb.append(key + "=" + mapParam.get(key));
		}
		return sb.toString();
    }
	
    /**
     * url串 转换成map.
     * @param URL
     * @return
     */
    public static Map<String, String> urlRequestToMap(String URL) { 
		Map<String, String> mapRequest = new HashMap<String, String>();
		String[] arrSplit = null;
		String strUrlParam = URL;
		if (strUrlParam == null) {
			return mapRequest;
		}
		// 每个键值为一组
		arrSplit = strUrlParam.split("[&]");
		for (String strSplit : arrSplit) {
			String[] arrSplitEqual = null;
			arrSplitEqual = strSplit.split("[=]");
			// 解析出键值
			if (arrSplitEqual.length > 1) {
				// 正确解析
				mapRequest.put(arrSplitEqual[0], arrSplitEqual[1]);
			} else {
				if (arrSplitEqual[0] != "") {
					// 只有参数没有值，不加入
					mapRequest.put(arrSplitEqual[0], "");
				}
			}
		}
		return mapRequest;
	}
    }
