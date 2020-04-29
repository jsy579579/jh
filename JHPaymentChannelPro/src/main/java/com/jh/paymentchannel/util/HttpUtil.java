package com.jh.paymentchannel.util;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * <p>
 * UrlConnection通讯辅助类
 * </p>
 * 
 * @Title: CallbackUtils.java
 * @Projectname: JAVA_JAR
 * @Package: com.yeepay.util
 * @Copyright: Copyright (c)2014
 * @Company: YeePay
 * @version: 1.0
 * @Create: 2014年4月2日
 */
public class HttpUtil {
	private static Logger log = LoggerFactory
			.getLogger(HttpUtil.class);
	/**
	 * 设置URL缓存时间为1小时
	 */
	static {
		System.setProperty("sun.net.inetaddr.ttl", "3600");
	}

	/**
	 * 默认字符编码
	 */
	public static final String DEFAULT_CHARSET = "UTF-8";

	public static final String HTTP_METHOD_POST = "POST";

	public static final String HTTP_METHOD_GET = "GET";

	/**
	 * 默认超时设置 (60秒)
	 */
	public static final int DEFAULT_TIMEOUT = 60000;

	/**
	 * 默认提交方式
	 */
	public static final String HTTP_METHOD_DEFAULT = "POST";

	public static final String HTTP_PREFIX = "http://";

	public static final String HTTPS_PREFIX = "https://";

	public static String httpRequest(String url, String method)
			throws Exception {
		return httpRequest(url, "", method, DEFAULT_CHARSET);
	}

	public static String httpRequest(String url, String queryString,
			String method) throws Exception {
		return httpRequest(url, queryString, method, DEFAULT_CHARSET);
	}

	public static String httpRequest(String url, Map params, String method)
			throws Exception {
		return httpRequest(url, params, method, DEFAULT_CHARSET);
	}

	public static String httpPost(String url, Map params) throws Exception {
		return httpRequest(url, params, HTTP_METHOD_POST, DEFAULT_CHARSET);
	}

	public static String httpPost(String url, String queryString)
			throws Exception {
		return httpRequest(url, queryString, HTTP_METHOD_POST, DEFAULT_CHARSET);
	}

	public static String httpGet(String url, Map params) throws Exception {
		return httpRequest(url, params, HTTP_METHOD_GET, DEFAULT_CHARSET);
	}

	public static String httpGet(String url, String queryString)
			throws Exception {
		return httpRequest(url, queryString, HTTP_METHOD_GET, DEFAULT_CHARSET);
	}

	/**
	 * 以建立HttpURLConnection方式发送请求
	 * 
	 * @param targetUrl
	 *            请求地址
	 * @param params
	 *            请求参数
	 * @param method
	 *            请求方式
	 * @param charSet
	 * @return 通讯失败返回null, 否则返回服务端输出
	 * @throws Exception
	 */
	public static String httpRequest(String url, Map params, String method,
			String charSet) throws Exception {
		String queryString = parseQueryString(params, charSet);
		return httpRequest(url, queryString, method, charSet);
	}

	/**
	 * 以建立HttpURLConnection方式发送请求
	 * 
	 * @param targetUrl
	 *            请求地址
	 * @param queryString
	 *            请求参数
	 * @param method
	 *            请求方式
	 * @param charSet
	 * @return 通讯失败返回null, 否则返回服务端输出
	 * @throws Exception
	 */
	public static String httpRequest(String targetUrl, String queryString,
			String method, String charSet) throws Exception {
		HttpURLConnection urlConn = null;
		URL destURL = null;
		boolean httpsFlag = false;
		if (targetUrl == null || targetUrl.trim().length() == 0) {
			throw new IllegalArgumentException("invalid targetUrl : "
					+ targetUrl);
		}
		targetUrl = targetUrl.trim();

		// if(targetUrl.toLowerCase().startsWith(HTTPS_PREFIX)){
		// throw new IllegalArgumentException("unsupport protocal : https");
		// }

		if (targetUrl.toLowerCase().startsWith(HTTPS_PREFIX)) {
			httpsFlag = true;
		} else if (!targetUrl.toLowerCase().startsWith(HTTP_PREFIX)) {
			targetUrl = HTTP_PREFIX + targetUrl;
		}

		// if(!targetUrl.toLowerCase().startsWith(HTTP_PREFIX) &&
		// !targetUrl.toLowerCase().startsWith(HTTPS_PREFIX)){
		// targetUrl = HTTP_PREFIX+targetUrl;
		// }
		if (queryString != null) {
			queryString = queryString.trim();
		}
		if (method == null
				|| !(method.equals(HTTP_METHOD_POST) || method
						.equals(HTTP_METHOD_GET))) {
			throw new IllegalArgumentException("invalid http method : "
					+ method);
		}

		String baseUrl = "";
		String params = "";
		String fullUrl = "";

		int index = targetUrl.indexOf("?");
		if (index != -1) {
			baseUrl = targetUrl.substring(0, index);
			params = targetUrl.substring(index + 1);
		} else {
			baseUrl = targetUrl;
		}
		if (queryString != null && queryString.trim().length() != 0) {
			if (params.trim().length() > 0) {
				params += "&" + queryString;
			} else {
				params += queryString;
			}
		}

		fullUrl = baseUrl + (params.trim().length() == 0 ? "" : ("?" + params));
		// logger.info("fullUrl:" + fullUrl);
		log.debug("请求URL:{}",fullUrl);
		System.out.println("SDK HTTP请求:" + fullUrl);
		StringBuffer result = new StringBuffer(2000);
		try {
			if (method.equals(HTTP_METHOD_POST)) {
				destURL = new URL(baseUrl);
				// destURL = new URL(null,baseUrl,new URLSHandler);
			} else {
				destURL = new URL(fullUrl);
				// destURL = new URL(null,fullUrl,new
				// sun.net.www.protocol.https.Handler());
			}
			if (httpsFlag) {
				trustAllHttpsCertificates();
				urlConn = (HttpsURLConnection) destURL.openConnection();
			} else {
				urlConn = (HttpURLConnection) destURL.openConnection();
			}
//			urlConn.setRequestProperty("Content-Type",
//					"application/x-www-form-urlencoded; charset=" + charSet);
			urlConn.setRequestProperty("Content-Type",
					"application/json; charset=" + charSet);//民生深圳分行,报文需要设置json格式请求,要验证是否满足其它上游
			urlConn.setDoOutput(true);
			urlConn.setDoInput(true);
			urlConn.setAllowUserInteraction(false);
			System.out.println(method);
			urlConn.setUseCaches(false);
			urlConn.setRequestMethod(method);
			urlConn.setConnectTimeout(DEFAULT_TIMEOUT);
			urlConn.setReadTimeout(DEFAULT_TIMEOUT);
			urlConn.setRequestProperty("User-Agent",
					"Mozilla/4.7 [en] (Win98; I)");

			if (method.equals(HTTP_METHOD_POST)) {
				OutputStream os = urlConn.getOutputStream();
				OutputStreamWriter osw = new OutputStreamWriter(os, charSet);
				osw.write(params);
				osw.flush();
				osw.close();
			}

			BufferedInputStream is = new BufferedInputStream(
					urlConn.getInputStream());
			BufferedReader br = new BufferedReader(new InputStreamReader(is,
					charSet));
			String temp = null;
			while ((temp = br.readLine()) != null) {
				result.append(temp);
				result.append("\n");
			}
			int responseCode = urlConn.getResponseCode();
			System.out.println("------------------ResponseCode[" + responseCode
					+ "],and targetUrl:[" + "targetUrl" + "?" + "queryString"
					+ "]" + "and result:[" + result + "]");
			if (responseCode != HttpURLConnection.HTTP_OK) {
				return null;
			}
			return result.toString();
		} catch (Exception e) {
			System.out.println("connection error : " + e.getMessage());
			throw e;

		} finally {
			if (urlConn != null) {
				urlConn.disconnect();
			}
		}
	}

	/**
	 * 根据查询返回结果分割获得Map
	 * 
	 * @param queryResult
	 * @param charSet
	 *            分隔符
	 * @return
	 */
	public static Map queryString(String queryResult, String splitChar) {

		String[] keyValuePairs = queryResult.split(splitChar);
		Map<String, String> map = new HashMap<String, String>();
		for (String keyValue : keyValuePairs) {
			if (keyValue.indexOf("=") == -1) {
				continue;
			}
			String[] args = keyValue.split("=");
			if (args.length == 2) {
				map.put(args[0], args[1]);
			}
			if (args.length == 1) {
				map.put(args[0], "");
			}
		}

		return map;
	}

	/**
	 * 把参数map转换成URL
	 * 
	 * @param params
	 * @param charSet
	 * @return
	 */
	public static String parseQueryString(Map params, String charSet) {
		if (null == params || params.keySet().size() == 0) {
			return "";
		}
		StringBuffer queryString = new StringBuffer(2000);
		for (Iterator i = params.keySet().iterator(); i.hasNext();) {
			String key = String.valueOf(i.next());
			Object obj = params.get(key);
			String value = "";
			if (obj != null) {
				value = obj.toString();
			}
			try {
				value = URLEncoder.encode(value, charSet);
			} catch (UnsupportedEncodingException ex) {
				System.out.println("encode url error: " + ex.getMessage());
			}
			queryString.append(key);
			queryString.append("=");
			queryString.append(value);
			queryString.append("&");
		}
		String result = queryString.toString();
		if (result.endsWith("&")) {
			result = result.substring(0, result.length() - 1);
		}
		return result;
	}

	public static String parseUrl(String targetUrl, String queryString) {
		if (targetUrl == null || targetUrl.trim().length() == 0) {
			throw new IllegalArgumentException("invalid targetUrl : "
					+ targetUrl);
		}
		targetUrl = targetUrl.trim();
		if (!targetUrl.toLowerCase().startsWith(HTTP_PREFIX)
				&& !targetUrl.toLowerCase().startsWith(HTTPS_PREFIX)) {
			targetUrl = HTTP_PREFIX + targetUrl;
		}

		if (queryString != null) {
			queryString = queryString.trim();
		}
		String baseUrl = "";
		String paramString = "";
		String fullUrl = "";
		int index = targetUrl.indexOf("?");
		if (index != -1) {
			baseUrl = targetUrl.substring(0, index);
			paramString = targetUrl.substring(index + 1);
		} else {
			baseUrl = targetUrl;
		}
		if (queryString != null && queryString.trim().length() != 0) {
			if (paramString.trim().length() > 0) {
				paramString += "&" + queryString;
			} else {
				paramString += queryString;
			}
		}
		fullUrl = baseUrl
				+ (paramString.trim().length() == 0 ? "" : ("?" + paramString));
		return fullUrl;
	}

	public static String parseUrl(String targetUrl, Map params, String charSet) {
		String queryString = parseQueryString(params, charSet);
		return parseUrl(targetUrl, queryString);
	}

	public static Map parseQueryString(String queryString) {
		if (queryString == null) {
			throw new IllegalArgumentException("queryString must be specified");
		}

		int index = queryString.indexOf("?");
		if (index > 0) {
			queryString = queryString.substring(index + 1);
		}

		String[] keyValuePairs = queryString.split("&");
		Map<String, String> map = new HashMap<String, String>();
		for (String keyValue : keyValuePairs) {
			if (keyValue.indexOf("=") == -1) {
				continue;
			}
			String[] args = keyValue.split("=");
			if (args.length == 2) {
				map.put(args[0], args[1]);
			}
			if (args.length == 1) {
				map.put(args[0], "");
			}
		}
		return map;
	}

	public static String parseUrl(String queryString) {
		if (queryString == null) {
			throw new IllegalArgumentException("queryString must be specified");
		}

		int index = queryString.indexOf("?");
		String targetUrl = null;
		if (index > 0) {
			targetUrl = queryString.substring(0, index);
		} else {
			targetUrl = queryString;
		}
		return targetUrl;
	}

	/**
	 * 把参数map转换成URL
	 * 
	 * @param params
	 * @param separator
	 *            分隔符
	 * @param charSet
	 * @return
	 */
	public static String parseQueryString(Map params, String separator,
			String charSet) {
		if (null == params || params.keySet().size() == 0) {
			return "";
		}
		StringBuffer queryString = new StringBuffer(2000);
		for (Iterator i = params.keySet().iterator(); i.hasNext();) {
			String key = String.valueOf(i.next());
			Object obj = params.get(key);
			String value = "";
			if (obj != null) {
				value = obj.toString();
			}
			try {
				value = URLEncoder.encode(value, charSet);
			} catch (UnsupportedEncodingException ex) {
				System.out.println("encode params error: " + ex.getMessage());
			}
			queryString.append(key);
			queryString.append("=");
			queryString.append(value);
			queryString.append(separator);
		}
		String result = queryString.toString();
		if (result.endsWith(separator)) {
			result = result.substring(0, result.length() - 1);
		}
		return result;
	}

	private static void trustAllHttpsCertificates() {
		try {
			// Create a trust manager that does not validate certificate chains:
			javax.net.ssl.TrustManager[] trustAllCerts = new javax.net.ssl.TrustManager[1];
			javax.net.ssl.TrustManager tm = new MyTrustManager();
			trustAllCerts[0] = tm;
			javax.net.ssl.SSLContext sc;
			sc = javax.net.ssl.SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, null);
			javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory(sc
					.getSocketFactory());
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (KeyManagementException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*public static void main(String[] args) throws Exception {
		System.out.println(HttpUtil.httpRequest(
				"http://localhost:9000/?name=test", "", "GET", "gbk"));
	}*/
}

class MyTrustManager implements javax.net.ssl.TrustManager,
		javax.net.ssl.X509TrustManager {
	public java.security.cert.X509Certificate[] getAcceptedIssuers() {
		return null;
	}

	public boolean isServerTrusted(java.security.cert.X509Certificate[] certs) {
		return true;
	}

	public boolean isClientTrusted(java.security.cert.X509Certificate[] certs) {
		return true;
	}

	public void checkServerTrusted(java.security.cert.X509Certificate[] certs,
			String authType) throws java.security.cert.CertificateException {
		return;
	}

	public void checkClientTrusted(java.security.cert.X509Certificate[] certs,
			String authType) throws java.security.cert.CertificateException {
		return;
	}
}