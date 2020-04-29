package com.jh.paymentgateway.util.xk;

import com.alibaba.fastjson.JSON;

import java.util.Map;

/**
 * 	json数据解析工具类
 * @author Administrator
 *
 */
public class JsonUtil {

	/**
	 * json字符串转换成map
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, String> jsonStringToMap(String jsonStr){
		return JSON.parseObject(jsonStr, Map.class);
	}
	
	/**
	 * json字符串转换成对象
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Object jsonStringToObj(String jsonStr, Class clazz){
		return JSON.parseObject(jsonStr, clazz);
	}
	
	/**
	 * 	对象转换成json字符串
	 * @return
	 */
	public static String ObjToJsonString(Object obj){
		return JSON.toJSONString(obj);
	}
	
	/**
	 * 	返回结果转换成map
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, Object> jsonResToMap(String jsonStr){
		return JSON.parseObject(jsonStr,Map.class);
	}
	
}
