package com.jh.paymentgateway.util.ap;


import com.jh.paymentgateway.util.ap.exception.CusRuntimeException;
import okhttp3.*;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InterruptedIOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

/**
* 类名称：OkHttpUtil
* 类描述：
* 创建人：dengzhixin
* 创建时间：2017年5月11日 下午2:29:18
* 版本：1.0
* 
*/

public class OkHttpUtil {

	private final static Logger LOG = LoggerFactory.getLogger(OkHttpUtil.class);

	public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

	public static final MediaType XML = MediaType.parse("application/xml; charset=utf-8");
	
	public static final MediaType TEXT = MediaType.parse("text/plain");
	
	public static final MediaType FORM = MediaType.parse("application/x-www-form-urlencoded; charset=UTF-8");
	
	public static final MediaType MULTIPART = MediaType.parse("multipart/form-data");
	
	private static final int MAP_TYPE = 1;

	private static final int TEXT_TYPE = 2;
	
	private static final int CONNECTION_TIMEOUT = 2 * 1000;

	private static OkHttpClient getHttpClient(int timeout, List<Interceptor> interceptorList) {
		OkHttpClient httpClient = new OkHttpClient.Builder()
				.connectTimeout(timeout, TimeUnit.SECONDS)
				.readTimeout(timeout, TimeUnit.SECONDS)
				.writeTimeout(timeout, TimeUnit.SECONDS)
				.build();
		if (interceptorList != null && interceptorList.size() > 0) {
			httpClient.networkInterceptors().addAll(interceptorList);
		}

		return httpClient;
	}

	private static OkHttpClient getHttpClient(int timeout) {
		OkHttpClient httpClient = new OkHttpClient.Builder()
				.connectTimeout(timeout, TimeUnit.SECONDS)
				.readTimeout(timeout, TimeUnit.SECONDS)
				.writeTimeout(timeout, TimeUnit.SECONDS)
				.build();
		return httpClient;
	}

	/**
	 * 初始http get请求参数，参数是以字母从小到大排列
	 * @param url
	 * @param paramMap
	 */
	private static Request buildGetUrlEndConnetorParam(String url, Map<String, Object> paramMap) {
		if (paramMap == null) {
			paramMap = new HashMap<String, Object>();
		}

		HttpUrl queryUrl = HttpUrl.parse(url);
		if (!paramMap.isEmpty()) {
			TreeMap<String, Object> treeMap = new TreeMap<String, Object>(paramMap);
			String key = treeMap.firstKey();
			Object value = treeMap.get(key);
			queryUrl = getQueryHttpUrl(key, value, queryUrl);
			while ((key = treeMap.higherKey(key)) != null) {
				value = treeMap.get(key);
				queryUrl = getQueryHttpUrl(key, value, queryUrl);
			}
		}

		return new Request.Builder().get().url(queryUrl).build();
	}

	/**
	 * 根据key, value组装GET请求URL地扯
	 * @param key
	 * @param value
	 * @param httpUrl
	 * @return
	 */
	private static HttpUrl getQueryHttpUrl(String key, Object value, HttpUrl httpUrl) {
		if (value != null) {
			if (value instanceof String) {
				httpUrl = httpUrl.newBuilder().addEncodedQueryParameter(key, (String) value).build();
			} else {
				httpUrl = httpUrl.newBuilder().addEncodedQueryParameter(key, String.valueOf(value)).build();
			}
		}

		return httpUrl;
	}

	/**
	 * 初始http get请求参数
	 * @param url
	 * @param paramMap
	 */
	private static Request buildPostUrlEndConnetorParam(String url, Map<String, Object> paramMap) {
		return buildUrlEndConnetorParam(url, paramMap, "POST");
	}

	/**
	 * 初始http请求参数
	 * @param url
	 * @param paramMap
	 */
	private static Request buildUrlEndConnetorParam(String url, Map<String, Object> paramMap, String method) {
		if (paramMap == null) {
			paramMap = new HashMap<String, Object>();
		}
		FormBody.Builder builder = new FormBody.Builder();
		for (Map.Entry<String, Object> entry : paramMap.entrySet()) {
			Object value = entry.getValue();
			// 不能将null数据加入到builder中，否则会报空指针异常
			if (value != null) {
				if (value instanceof String) {
					builder.add(entry.getKey(), (String) entry.getValue());
				} else {
					builder.add(entry.getKey(), String.valueOf(entry.getValue()));
				}
			}

		}
		RequestBody requestBody = builder.build();
		return new Request.Builder().method(method, requestBody).url(url).build();
	}

	/**
	 * 初始http post请求参数
	 * @param url
	 * @param jsonString
	 * @return
	 */
	private static Request buildJsonParam(String url, String jsonString) {
		RequestBody body = RequestBody.create(JSON, jsonString);
		return new Request.Builder().url(url).post(body).build();
	}

	/**
	 * 初始http post请求参数
	 * @param url
	 * @param paramMap
	 * @return
	 */
	private static Request buildJsonParam(String url, Map<String, Object> paramMap) {
		String jsonString = GsonUtil.getInstance().toJson(paramMap);
		return buildJsonParam(url, jsonString);
	}

	/**
	 * 初始http put请求参数
	 * @param url
	 * @param jsonString
	 * @return
	 */
	private static Request buildPutJsonParam(String url, String jsonString) {
		RequestBody body = RequestBody.create(JSON, jsonString);
		return new Request.Builder().method("PUT", body).url(url).build();
	}

	/**
	 * 初始http put请求参数
	 * @param url
	 * @param paramMap
	 * @return
	 */
	private static Request buildPutJsonParam(String url, Map<String, Object> paramMap) {
		String jsonString = GsonUtil.getInstance().toJson(paramMap);
		return buildPutJsonParam(url, jsonString);
	}

	@SuppressWarnings("unchecked")
	private static Object newHttpClient(Request request, int type, int timeout, List<Interceptor> interceptorList)
			throws Exception {
		Response response = null;
		try {
			OkHttpClient httpClient = getHttpClient(timeout, interceptorList);
			response = httpClient.newCall(request).execute();
			if (response.isSuccessful()) {
				String respTxt = response.body().string();
				if (MAP_TYPE == type) {
					Map<String, Object> result = null;
					result = GsonUtil.gsonToMaps(respTxt);
					return result;
				}

				return respTxt;
			} else {
				LOG.error("http has response error! reposne code is " + response.code());
				throw new CusRuntimeException("" + response.code(),
						"newHttpClient has response error!reposne code is " + response.code());
			}
		} catch (IllegalStateException e) {
			LOG.error("http has illegal state exception. " + e.getMessage(), e);
			throw new CusRuntimeException("" + 100, "newHttpClient IllegalStateException");
		} catch (InterruptedIOException e) {
			LOG.error("http time out, " + e.getMessage(), e);
			throw e;
		} finally {
			if (response != null) {
				response.body().close();
			}
		}

	}

	@SuppressWarnings("unchecked")
	private static Object newHttpClient(Request request, int type, int timeout) throws Exception {
		Response response = null;
		try {
			OkHttpClient httpClient = getHttpClient(timeout);
			response = httpClient.newCall(request).execute();
			if (response.isSuccessful()) {
				String respTxt = response.body().string();
				if (MAP_TYPE == type) {
					Map<String, Object> result = null;
					result = GsonUtil.gsonToMaps(respTxt);
					return result;
				}

				return respTxt;
			} else {
				LOG.error("http has response error! reposne code is " + response.code());
				throw new CusRuntimeException("" + response.code(),
						"newHttpClient has response error!reposne code is " + response.code());
			}
		} catch (IllegalStateException e) {
			LOG.error("http has illegal state exception. " + e.getMessage(), e);
			throw new CusRuntimeException("" + 100, "newHttpClient IllegalStateException");
		} catch (InterruptedIOException e) {
			LOG.error("http time out, " + e.getMessage(), e);
			throw e;
		} finally {
			if (response != null) {
				response.body().close();
			}
		}

	}

	/**
	 * http get方式请求，并返回String响应报文
	* @param url
	* @param paramMap
	* @param timeout 单位：秒
	* @return
	* @throws Exception
	*/
	public static String httpClientGetReturnAsString(String url, Map<String, Object> paramMap, int timeout,
			List<Interceptor> interceptorList) throws Exception {
		Request request = buildGetUrlEndConnetorParam(url, paramMap);
		Object object = newHttpClient(request, TEXT_TYPE, timeout, interceptorList);
		return object != null ? (String) object : null;
	}

	/**
	 * http get方式请求，并返回String响应报文
	* @param url
	* @param paramMap
	* @param timeout 单位：秒
	* @return
	* @throws Exception
	*/
	public static String httpClientGetReturnAsString(String url, Map<String, Object> paramMap, int timeout)
			throws Exception {
		Request request = buildGetUrlEndConnetorParam(url, paramMap);
		Object object = newHttpClient(request, TEXT_TYPE, timeout);
		return object != null ? (String) object : null;
	}

	/**
	 * http get方式请求，并返回Map响应报文
	 * @param url
	 * @param paramMap
	 * @param timeout
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, Object> httpClientGetReturnAsMap(String url, Map<String, Object> paramMap, int timeout,
			List<Interceptor> interceptorList) throws Exception {
		Request request = buildGetUrlEndConnetorParam(url, paramMap);
		Object object = newHttpClient(request, MAP_TYPE, timeout, interceptorList);
		return object != null ? (Map<String, Object>) object : null;
	}

	/**
	 * http get方式请求，并返回Map响应报文
	 * @param url
	 * @param paramMap
	 * @param timeout
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, Object> httpClientGetReturnAsMap(String url, Map<String, Object> paramMap, int timeout)
			throws Exception {
		Request request = buildGetUrlEndConnetorParam(url, paramMap);
		Object object = newHttpClient(request, MAP_TYPE, timeout);
		return object != null ? (Map<String, Object>) object : null;
	}

	/**
	 * 同步的Post请求 
	 * @param url
	 * @param jsonString
	 * @param timeout
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, Object> httpClientJsonPostReturnAsMap(String url, String jsonString, int timeout,
			List<Interceptor> interceptorList) throws Exception {
		Request request = buildJsonParam(url, jsonString);

		Object object = newHttpClient(request, MAP_TYPE, timeout, interceptorList);
		return object != null ? (Map<String, Object>) object : null;
	}

	/**
	 * 同步的Post请求 
	 * @param url
	 * @param jsonString
	 * @param timeout
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, Object> httpClientJsonPostReturnAsMap(String url, String jsonString, int timeout)
			throws Exception {
		Request request = buildJsonParam(url, jsonString);

		Object object = newHttpClient(request, MAP_TYPE, timeout);
		return object != null ? (Map<String, Object>) object : null;
	}

	/**
	 * http post方式请求，并返回Map响应报文
	 * @param url
	 * @param jsonString
	 * @param timeout 单位：秒
	 * @return
	 */
	public static String httpClientJsonPostReturnAsString(String url, String jsonString, int timeout,
			List<Interceptor> interceptorList) throws Exception {
		Request request = buildJsonParam(url, jsonString);
		return (String) newHttpClient(request, TEXT_TYPE, timeout, interceptorList);
	}

	/**
	 * http post方式请求，并返回Map响应报文
	 * @param url
	 * @param jsonString
	 * @param timeout 单位：秒
	 * @return
	 */
	public static String httpClientJsonPostReturnAsString(String url, String jsonString, int timeout) throws Exception {
		Request request = buildJsonParam(url, jsonString);
		return (String) newHttpClient(request, TEXT_TYPE, timeout);
	}

	/**
	 * http post方式请求，并返回Map响应报文
	 * @param url
	 * @param paramMap
	 * @param timeout 单位：秒
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, Object> httpClientJsonPostReturnAsMap(String url, Map<String, Object> paramMap,
			int timeout, List<Interceptor> interceptorList) throws Exception {
		Request request = buildJsonParam(url, paramMap);
		Object object = newHttpClient(request, MAP_TYPE, timeout, interceptorList);
		return object != null ? (Map<String, Object>) object : null;
	}

	/**
	 * http post方式请求，并返回Map响应报文
	 * @param url
	 * @param paramMap
	 * @param timeout 单位：秒
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, Object> httpClientJsonPostReturnAsMap(String url, Map<String, Object> paramMap,
			int timeout) throws Exception {
		Request request = buildJsonParam(url, paramMap);
		Object object = newHttpClient(request, MAP_TYPE, timeout);
		return object != null ? (Map<String, Object>) object : null;
	}

	/**
	 * http post方式请求，并返回Map响应报文
	 * @param url
	 * @param paramMap
	 * @param timeout 单位：秒
	 * @return
	 */
	public static String httpClientJsonPostReturnAsString(String url, Map<String, Object> paramMap, int timeout,
			List<Interceptor> interceptorList) throws Exception {
		Request request = buildJsonParam(url, paramMap);
		return (String) newHttpClient(request, TEXT_TYPE, timeout, interceptorList);
	}

	/**
	 * http post方式请求，并返回Map响应报文
	 * @param url
	 * @param paramMap
	 * @param timeout 单位：秒
	 * @return
	 */
	public static String httpClientJsonPostReturnAsString(String url, Map<String, Object> paramMap, int timeout)
			throws Exception {
		Request request = buildJsonParam(url, paramMap);
		return (String) newHttpClient(request, TEXT_TYPE, timeout);
	}

	/**
	 * http put方式请求，并返回Map响应报文
	 * @param url
	 * @param jsonString
	 * @param timeout 单位：秒
	 * @return
	 */
	public static String httpClientJsonPutReturnAsString(String url, String jsonString, int timeout,
			List<Interceptor> interceptorList) throws Exception {
		Request request = buildPutJsonParam(url, jsonString);
		return (String) newHttpClient(request, TEXT_TYPE, timeout, interceptorList);
	}

	/**
	 * http put方式请求，并返回Map响应报文
	 * @param url
	 * @param jsonString
	 * @param timeout 单位：秒
	 * @return
	 */
	public static String httpClientJsonPutReturnAsString(String url, String jsonString, int timeout) throws Exception {
		Request request = buildPutJsonParam(url, jsonString);
		return (String) newHttpClient(request, TEXT_TYPE, timeout);
	}

	/**
	 * http put方式请求，并返回String响应报文
	 * @param url
	 * @param paramMap
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, Object> httpClientJsonPutReturnAsString(String url, Map<String, Object> paramMap,
			int timeout, List<Interceptor> interceptorList) throws Exception {
		Request request = buildPutJsonParam(url, paramMap);
		Object object = newHttpClient(request, MAP_TYPE, timeout, interceptorList);
		return object != null ? (Map<String, Object>) object : null;
	}

	/**
	 * http put方式请求，并返回String响应报文
	 * @param url
	 * @param paramMap
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, Object> httpClientJsonPutReturnAsString(String url, Map<String, Object> paramMap,
			int timeout) throws Exception {
		Request request = buildPutJsonParam(url, paramMap);
		Object object = newHttpClient(request, MAP_TYPE, timeout);
		return object != null ? (Map<String, Object>) object : null;
	}

	/**
	 * http post方式请求，并返回String响应报文
	 * @param url
	 * @param paramMap
	 * @param timeout 单位：秒
	 * @return
	 */
	public static String httpClientPostReturnJson(String url, Map<String, Object> paramMap, int timeout,
			List<Interceptor> interceptorList) throws Exception {
		Request request = buildPostUrlEndConnetorParam(url, paramMap);
		return (String) newHttpClient(request, TEXT_TYPE, timeout, interceptorList);

	}

	/**
	 * http post方式请求，并返回String响应报文
	 * @param url
	 * @param paramMap
	 * @param timeout 单位：秒
	 * @return
	 */
	public static String httpClientPostReturnJson(String url, Map<String, Object> paramMap, int timeout)
			throws Exception {
		Request request = buildPostUrlEndConnetorParam(url, paramMap);
		return (String) newHttpClient(request, TEXT_TYPE, timeout);

	}

	/**
	 * http post方式提交form请求，并返回map响应报文
	 * @param url
	 * @param paramMap
	 * @param timeout 单位：秒
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, Object> httpClientPostFormReturnMap(String url, Map<String, Object> paramMap, int timeout,
			List<Interceptor> interceptorList) throws Exception {
		Request request = buildPostUrlEndConnetorParam(url, paramMap);
		Object object = newHttpClient(request, MAP_TYPE, timeout, interceptorList);
		return object != null ? (Map<String, Object>) object : null;

	}

	/**
	 * http post方式提交form请求，并返回map响应报文
	 * @param url
	 * @param paramMap
	 * @param timeout 单位：秒
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, Object> httpClientPostFormReturnMap(String url, Map<String, Object> paramMap, int timeout)
			throws Exception {
		Request request = buildPostUrlEndConnetorParam(url, paramMap);
		Object object = newHttpClient(request, MAP_TYPE, timeout);
		return object != null ? (Map<String, Object>) object : null;

	}

	/**
	 * xml 请求
	 * 
	 * @param url
	 * @param obj
	 * @return
	 * @throws Exception
	 */
	public static String clientPostXmlRetTxt(String url, String xml, int timeout, List<Interceptor> interceptorList)
			throws Exception {
		if (StringUtils.isEmpty(xml)) {
			return null;
		}

		Request request = buildPostXmlParam(url, xml);
		return (String) newHttpClient(request, TEXT_TYPE, timeout, interceptorList);
	}

	/**
	 * xml 请求
	 * 
	 * @param url
	 * @param obj
	 * @return
	 * @throws Exception
	 */
	public static String clientPostXmlRetTxt(String url, String xml, int timeout) throws Exception {
		if (StringUtils.isEmpty(xml)) {
			return null;
		}

		Request request = buildPostXmlParam(url, xml);
		return (String) newHttpClient(request, TEXT_TYPE, timeout);
	}
	
	/**
	 * xml 请求
	 * 
	 * @param url
	 * @param obj
	 * @return
	 * @throws Exception
	 */
	public static String clientPostTxtRetTxt(String url, String text) throws Exception {
		if (StringUtils.isEmpty(text)) {
			return null;
		}

		Request request = buildPostTextParam(url, text);
		return (String) newHttpClient(request, TEXT_TYPE, CONNECTION_TIMEOUT);
	}

	/**
	 * 初始http post请求xml参数
	 * @param url
	 * @param xmlString
	 * @return
	 */
	private static Request buildPostXmlParam(String url, String xmlString) {
		if (StringUtils.isEmpty(xmlString)) {
			return new Request.Builder().url(url).build();
		}

		RequestBody body = RequestBody.create(XML, xmlString);
		return new Request.Builder().method("POST", body).url(url).build();
	}
	
	/**
	 * 初始http post请求text参数
	 * @param url
	 * @param text
	 * @return
	 */
	private static Request buildPostTextParam(String url, String text) {
		if (StringUtils.isEmpty(text)) {
			return new Request.Builder().url(url).build();
		}

		RequestBody body = RequestBody.create(TEXT, text);
		return new Request.Builder().method("POST", body).url(url).build();
	}
	
}
