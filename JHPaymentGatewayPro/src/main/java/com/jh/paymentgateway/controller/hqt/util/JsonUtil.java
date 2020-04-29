package com.jh.paymentgateway.controller.hqt.util;

import com.alibaba.fastjson.JSON;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

public class JsonUtil {
	
	public static Map jsonToMap(String json) { 		
		Map map = (Map) JSON.parse(json);
		return map;
	}

	public static String map2Json(Map map) {
		return JSON.toJSONString(map);
	}
	

	public static String sortMap2Json(Map map) {
		return JSON.toJSONString(sortMapByKey(map));
	}
	/**
     * 使用 Map按key进行排序
     * @param map
     * @return
     */
    public static Map<String, Object> sortMapByKey(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }

        Map<String, Object> sortMap = new TreeMap<String, Object>(new Comparator<String>(){
        	
            public int compare(String str1, String str2) {

                return str1.compareTo(str2);
            }
        });

        sortMap.putAll(map);

        return sortMap;
    }
    public static String toJSONString(Object obj) {
		return JSON.toJSONString(obj);
	}

	/**
	 * json字符串转换为javabean
	 * 
	 * @param json
	 * @param clazz
	 * @return
	 */
	public static final <T> T parseObject(String json, Class<T> clazz) {
		return JSON.parseObject(json, clazz);
	}
}

