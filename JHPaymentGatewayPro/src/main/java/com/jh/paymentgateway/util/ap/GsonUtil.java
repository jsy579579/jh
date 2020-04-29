package com.jh.paymentgateway.util.ap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang.StringUtils;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
* 创建人：dengzhixin   
* 创建时间：2016年3月10日 下午2:11:47   
* 修改人： 
* 修改时间：
* 修改备注：   
* @version    
*    
*/

public class GsonUtil {

	private static final Gson GSON = new GsonBuilder().create();
	private static final Gson DateFormatGSON = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();

	private GsonUtil() {
	}

	public static GsonUtil getInstance() {
		return GsonUtilHolder.INSTANCE;
	}

	/** 
	 * 将object对象转成json字符串 
	 *  
	 * @param object 
	 * @return 
	 */
	public <T> String toJson(T t) {
		return GSON.toJson(t);
	}
	
	/** 
	 * 将object对象转成json字符串 
	 *  
	 * @param object 
	 * @return 
	 */
	public <T> String toDateFormatGSONJson(T t) {
		return DateFormatGSON.toJson(t);
	}

	/** 
	 * 将gsonString转成泛型bean
	 * @param json 
	 * @param clazz 
	 * @param <T> 
	 * @return 
	 */
	@SuppressWarnings("unchecked")
	public <T> T fromJson(String json, Class<T> clazz) {
		if (StringUtils.isBlank(json)) {
			return null;
		}
		return GSON.fromJson(json, clazz);
	}

	/** 
	* 转成list 
	* 泛型在编译期类型被擦除导致报错 
	* @param gsonString 
	* @param cls 
	* @return 
	*/
	public static <T> List<T> gsonToList(String json, Class<T> clazz) {
		List<T> list = null;
		if (GSON != null) {
			list = GSON.fromJson(json, new TypeToken<List<T>>() {
			}.getType());
		}
		return list;
	}

	/** 
	 * 转成map的 
	 *  
	 * @param json 
	 * @return 
	 */
	public static <T> Map<String, T> gsonToMaps(String json) {
		Map<String, T> map = null;
		if (json != null) {
			map = GSON.fromJson(json, new TypeToken<Map<String, T>>() {
			}.getType());
		}
		return map;
	}
	
	/** 
	 * 转成指定类型map的
	 *  
	 * @param json 
	 * @return 
	 */
	public static <T> Map<String, T> gsonToTypeMaps(String json, Type type) {
		Map<String, T> map = null;
		if (json != null && type != null) {
			map = GSON.fromJson(json, type);
		} else {
			map = gsonToMaps(json);
		}
		return map;
	}

	/** 
	 * 转成list中有map的 
	 *  
	 * @param json 
	 * @return 
	 */
	public static <T> List<Map<String, T>> gsonToListMaps(String json) {
		List<Map<String, T>> list = null;
		if (GSON != null) {
			list = GSON.fromJson(json, new TypeToken<List<Map<String, T>>>() {
			}.getType());
		}
		return list;
	}

	private static class GsonUtilHolder {
		private static final GsonUtil INSTANCE = new GsonUtil();
	}
}
