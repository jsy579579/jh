package com.jh.paymentchannel.util;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

import net.sf.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * json字符串 和对象互换 工具类 依赖Gson包
 * 
 * @see com.google.gson.Gson
 * 
 */
public class JsonUtil {

	/**
	 * 将json字符串转换成对象
	 * 
	 * @param json
	 * @param type
	 * @return
	 */
	public static <T> T parse(String json, Class<T> type) {
		Gson gson = new GsonBuilder().serializeNulls().create();
		T t = null;
		try {
			t = gson.fromJson(json, type);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		return t;
	}

	/**
	 * 将json转成数组
	 * 
	 * @param json
	 * @param type
	 * @return
	 */
	public static <T> T[] parseArr(String json, Class<T[]> type) {
		return parse(json, type);
	}

	/**
	 * 将json转成集合
	 * 
	 * @param json
	 * @param type
	 * @return
	 */
	public static <T> ArrayList<T> parseList(String json, Class<T[]> type) {
		return new ArrayList<T>(Arrays.asList(parse(json, type)));
	}

	/**
	 * 将对象转成json字符串
	 * 
	 * @param o
	 * @return
	 */
	public static String format(Object o) {
		Gson gson = new Gson();
		return gson.toJson(o);
	}

	/**
	 * 将对象转成json字符串 并使用url编码
	 * 
	 * @param o
	 * @return
	 */
	public static String formatURLString(Object o) {
		try {
			return URLEncoder.encode(format(o), "utf-8");
		} catch (Exception e) {
			return null;
		}
	}

	public static HashMap<String, Object> toHashMap2(Object object) {
		HashMap<String, Object> data = new HashMap<String, Object>();
		// 将json字符串转换成jsonObject
		JSONObject jsonObject = JSONObject.fromObject(object);
		Iterator it = jsonObject.keys();
		// 遍历jsonObject数据，添加到Map对象
		while (it.hasNext()) {
			String key = String.valueOf(it.next());
			String value = (String) jsonObject.get(key);
			data.put(key, value);
		}
		return data;
	}

	public static HashMap<String, String> toHashMap(Object object) {
		HashMap<String, String> data = new HashMap<String, String>();
		// 将json字符串转换成jsonObject
		JSONObject jsonObject = JSONObject.fromObject(object);
		Iterator it = jsonObject.keys();
		// 遍历jsonObject数据，添加到Map对象
		while (it.hasNext()) {
			String key = String.valueOf(it.next());
			String value = (String) jsonObject.get(key);
			data.put(key, value);
		}
		return data;
	}
}
