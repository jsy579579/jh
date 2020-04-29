package com.jh.paymentgateway.util.yf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONNull;
import net.sf.json.JSONObject;

public class JSONUtil {
	/**
	 * @Title: packJson
	 * @Description: 打包请求的json
	 * @param request
	 * @return String 返回类型
	 * @throws
	 */
	public static String packJson(Object object) throws Exception {
		JSONObject jsonObject = JSONObject.fromObject(object);
		return jsonObject.toString();
	}

	/**
	 * @Title: parseJson
	 * @Description: 解析json
	 * @param json
	 * @return
	 * @throws Exception
	 *             设定文件
	 * @return Object 返回类型
	 * @throws
	 */
	public static Object parseJson(String json, Class<?> clz) throws Exception {
		JSONObject jsonObject = JSONObject.fromObject(json);
		Object object = JSONObject.toBean(jsonObject, clz);
		return object;
	}

	/**
	 * @Title: parseJson2
	 * @Description: 解析json
	 * @param json
	 * @return
	 * @throws Exception
	 *             设定文件
	 * @return JSONObject 返回类型
	 * @throws
	 */
	public static JSONObject parseJson2(String json) throws Exception {
		JSONObject jsonObject = JSONObject.fromObject(json);
		return jsonObject;
	}
	
	public static Map<String, Object> jsonToMap(String json) {
		Map<String, Object> classMap = new HashMap<String, Object>();
		classMap.put("map", Map.class);
		@SuppressWarnings("unchecked")
		Map<String, Object> map = (Map<String, Object>) JSONObject.toBean(JSONObject.fromObject(json), Map.class, classMap);
		// 转换null
		Iterator<String> it = map.keySet().iterator();
		while (it.hasNext()) {
			String key = (String) it.next();
			Object value = map.get(key);
			if (value instanceof JSONNull) {
				map.put(key, null);
			}
		}
		return map;
	}

	/**
	 * 将jsonString转成Map
	 * 
	 * @param jsonStr
	 * @return
	 */
	public static Map<String, Object> parseJSON2Map(String jsonStr) {
		Map<String, Object> map = new HashMap<String, Object>();
		// 最外层解析
		JSONObject json = JSONObject.fromObject(jsonStr);
		for (Object k : json.keySet()) {
			Object v = json.get(k);
			// 如果内层还是数组的话，继续解析
			if (v instanceof JSONArray) {
				List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
				@SuppressWarnings("unchecked")
				Iterator<JSONObject> it = ((JSONArray) v).iterator();
				while (it.hasNext()) {
					JSONObject json2 = it.next();
					list.add(parseJSON2Map(json2.toString()));
				}
				map.put(k.toString(), list);
			} else {
				map.put(k.toString(), String.valueOf(v));
			}
		}
		return map;
	}

	/**
	 * MAP转JSON字符串
	 * 
	 * @param map
	 * @return
	 */
	public static String mapToJson(Map<String, Object> map) {
		Set<String> keys = map.keySet();
		String key = "";
		Object value = "";
		StringBuffer jsonBuffer = new StringBuffer();
		jsonBuffer.append("{");
		for (Iterator<String> it = keys.iterator(); it.hasNext();) {
			key = (String) it.next();
			value = map.get(key);
			jsonBuffer.append(key + ":" + "\"" + value + "\"");
			if (it.hasNext()) {
				jsonBuffer.append(",");
			}
		}
		jsonBuffer.append("}");
		return jsonBuffer.toString();
	}
}
