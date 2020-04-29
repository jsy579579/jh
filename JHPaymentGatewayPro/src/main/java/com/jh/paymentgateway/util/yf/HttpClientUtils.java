package com.jh.paymentgateway.util.yf;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import net.sf.json.JSONObject;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 通讯类 依赖的jar包：commons-lang-2.6.jar、httpclient-4.3.2.jar、httpcore-4.3.1.jar、
 * commons-io-2.4.jar
 * 
 * @author liy
 */
@SuppressWarnings("all")
public class HttpClientUtils {
	public static final int connTimeout = 3000000;
	public static final int readTimeout = 3000000;
	public static final String URL_PARAM_DECODECHARSET_UTF8 = "UTF-8";
	public static final String URL_PARAM_DECODECHARSET_GBK = "GBK";
	private static final String CONTENT_TYPE_TEXT_JSON = "text/json";
	private static final String APPLICATION_STANDARD = "application/x-www-form-urlencoded";

	private static HttpClient client = null;

	static {
		// 连接池，管理着很多http链接 当请求一个新的连接时，
		// 如果连接池有有可用的持久连接，连接管理器就会使用其中的一个，而不是再创建一个新的连接
		PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
		// 将最大连接数增加到128
		cm.setMaxTotal(128);
		// 将每个路由基础的连接增加到20
		cm.setDefaultMaxPerRoute(128);
		// 将目标主机的最大连接数增加到50
		// HttpHost localhost = new HttpHost("www.yeetrack.com", 80);
		// cm.setMaxPerRoute(new HttpRoute(localhost), 50);
		client = HttpClients.custom().setConnectionManager(cm).build();
	}

	public static byte[] postBinResource(String urlStr, String request, String encode, int timeOutInSeconds) throws Exception {
		HttpURLConnection http = null;
		InputStream inputStream = null;
		OutputStream outputStream = null;

		try {
			URL url = new URL(urlStr);
			http = (HttpURLConnection) url.openConnection();
			http.setDoInput(true);
			http.setDoOutput(true);
			http.setUseCaches(false);
			http.setConnectTimeout(timeOutInSeconds * 1000);// 设置连接超时
			// 如果在建立连接之前超时期满，则会引发一个
			// java.net.SocketTimeoutException。超时时间为零表示无穷大超时。
			http.setReadTimeout(timeOutInSeconds * 1000);// 设置读取超时
			// 如果在数据可读取之前超时期满，则会引发一个
			// java.net.SocketTimeoutException。超时时间为零表示无穷大超时。
			http.setRequestMethod("POST");
			http.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=" + encode);
			http.connect();

			outputStream = http.getOutputStream();
			OutputStreamWriter osw = new OutputStreamWriter(outputStream, encode);
			osw.write(request);
			osw.flush();
			osw.close();

			if (http.getResponseCode() == 200) {
				inputStream = http.getInputStream();
				byte[] returnValue1 = IOUtils.toByteArray(inputStream);
				return returnValue1;
			} else {
				throw new RuntimeException("http read [" + http.getResponseCode() + "]");
			}
		} catch (Exception e) {
			throw e;
		} finally {
			if (http != null)
				http.disconnect();
			IOUtils.closeQuietly(inputStream);
			IOUtils.closeQuietly(outputStream);
		}
	}

	/**
	 * @param httpUrl
	 *            URL
	 * @param Method
	 *            请求方式 get/post
	 * @param httpArg
	 *            请求参数
	 * @param params
	 * @return
	 */
	public static String request(String httpUrl, String Method, String httpArg, Map<String, String> params) {
		BufferedReader reader = null;
		String result = null;
		StringBuffer sbf = new StringBuffer();
		if ("GET".equalsIgnoreCase(Method)) {
			httpUrl = httpUrl + "?" + httpArg;
		}
		try {
			URL url = new URL(httpUrl);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod(Method);// 请求方式
			Set<Entry<String, String>> entrySet = params.entrySet();

			if (params != null && !params.isEmpty()) {
				for (Entry<String, String> entry : entrySet) {
					connection.setRequestProperty(entry.getKey(), entry.getValue());
				}
			}
			if ("POST".equalsIgnoreCase(Method)) {
				connection.setDoOutput(true);
				connection.getOutputStream().write(httpArg.getBytes("UTF-8"));
			}
			connection.connect();
			InputStream is = connection.getInputStream();
			reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			String strRead = null;
			while ((strRead = reader.readLine()) != null) {
				sbf.append(strRead);
				sbf.append("\r\n");
			}
			reader.close();
			result = sbf.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * post请求
	 * 
	 * @param url
	 *            url请求地址
	 * @param info
	 *            请求参数名
	 * @param json
	 *            请求参数
	 * @return
	 * @throws Exception
	 */
	public static String postJson(String reqUrl, String info, String json) throws Exception {
		URL url = new URL(reqUrl);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setDoOutput(true);
		connection.setDoInput(true);
		connection.setRequestMethod("POST");
		connection.setUseCaches(false);
		connection.setInstanceFollowRedirects(true);
		connection.setConnectTimeout(120000);// 2分钟
		connection.setReadTimeout(120000);
		connection.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
		connection.connect();
		DataOutputStream out = new DataOutputStream(connection.getOutputStream());
		String content = URLEncoder.encode(json, "utf-8");// 传入加密后的jsonString
		if (info != null && !"".equals(info)) {
			content = info + "=" + content;// 传入加密后的jsonString
		}
		out.writeBytes(content);
		out.flush();
		out.close();
		BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
		StringBuffer buffer1 = new StringBuffer();
		String line;
		while ((line = reader.readLine()) != null) {
			buffer1.append(line);
		}
		reader.close();
		return buffer1.toString();
	}

	/**
	 * Post请求发送 (JSON)
	 * 
	 * @param url
	 * @param String
	 *            json
	 * @return ResponseBody, 使用指定的字符集编码.
	 * @throws ConnectTimeoutException
	 *             建立链接超时异常
	 */
	public static String postParametersWithJson(String url, String json) throws Exception {
		return postForm(url, json, connTimeout, readTimeout);
	}

	/**
	 * Post请求发送 (String)
	 * 
	 * @param url
	 * @param parameterStr
	 * @return ResponseBody, 使用指定的字符集编码.
	 * @throws ConnectTimeoutException
	 *             建立链接超时异常
	 * @throws SocketTimeoutException
	 *             响应超时
	 * @throws Exception
	 */
	public static String postParameters(String url, String parameterStr) throws ConnectTimeoutException, SocketTimeoutException, Exception {
		return post(url, parameterStr, APPLICATION_STANDARD, URL_PARAM_DECODECHARSET_UTF8, connTimeout, readTimeout);
	}

	/**
	 * Post请求发送 (String)
	 * 
	 * @param url
	 * @param parameterStr
	 * @param mimeType
	 *            例如 application/xml "application/x-www-form-urlencoded"
	 *            a=1&b=2&c=3
	 * @param charset
	 *            编码
	 * @param connTimeout
	 *            建立链接超时时间,毫秒.
	 * @param readTimeout
	 *            响应超时时间,毫秒.
	 * @return ResponseBody, 使用指定的字符集编码.
	 * @throws ConnectTimeoutException
	 *             建立链接超时异常
	 * @throws SocketTimeoutException
	 *             响应超时
	 * @throws Exception
	 */
	public static String postParameters(String url, String parameterStr, String charset, Integer connTimeout, Integer readTimeout)
			throws ConnectTimeoutException, SocketTimeoutException, Exception {
		return post(url, parameterStr, APPLICATION_STANDARD, charset, connTimeout, readTimeout);
	}

	/**
	 * Post请求发送 (Map)
	 * 
	 * @param url
	 * @param params
	 *            Map
	 * @param headers
	 *            Map
	 * @return ResponseBody, 使用指定的字符集编码.
	 * @throws ConnectTimeoutException
	 *             建立链接超时异常
	 * @param boolean 使用传值方法
	 */
	public static String postParameters(String url, Map<String, String> params, Map<String, String> headers, boolean way)
			throws ConnectTimeoutException, SocketTimeoutException, Exception {
		return postForm(url, params, headers, connTimeout, readTimeout, way);
	}

	/**
	 * Post请求发送 (Map)
	 * 
	 * @param url
	 * @param params
	 *            Map
	 * @param mimeType
	 *            例如 application/xml "application/x-www-form-urlencoded"
	 *            a=1&b=2&c=3
	 * @param connTimeout
	 *            建立链接超时时间,毫秒.
	 * @param readTimeout
	 *            响应超时时间,毫秒.
	 * @return ResponseBody, 使用指定的字符集编码.
	 * @throws ConnectTimeoutException
	 *             建立链接超时异常
	 * @throws SocketTimeoutException
	 *             响应超时
	 * @throws Exception
	 */
	public static String postParameters(String url, Map<String, String> params, Integer connTimeout, Integer readTimeout)
			throws ConnectTimeoutException, SocketTimeoutException, Exception {
		return postForm(url, params, null, connTimeout, readTimeout, true);
	}

	/**
	 * get请求发送
	 * 
	 * @param url
	 * @return ResponseBody, 使用指定的字符集编码.
	 * @throws ConnectTimeoutException
	 *             建立链接超时异常
	 * @throws SocketTimeoutException
	 *             响应超时
	 * @throws Exception
	 */
	public static String get(String url) throws Exception {
		return get(url, URL_PARAM_DECODECHARSET_UTF8, null, null);
	}

	/**
	 * get请求发送
	 * 
	 * @param url
	 * @param charset
	 *            编码
	 * @return ResponseBody, 使用指定的字符集编码.
	 * @throws ConnectTimeoutException
	 *             建立链接超时异常
	 * @throws SocketTimeoutException
	 *             响应超时
	 * @throws Exception
	 */
	public static String get(String url, String charset) throws Exception {
		return get(url, charset, connTimeout, readTimeout);
	}

	/**
	 * 向指定 URL 发送POST方法的请求
	 * 
	 * @param url
	 *            发送请求的 URL
	 * @param param
	 *            请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
	 * @return 所代表远程资源的响应结果
	 */
	public static String sendPost(String url, String param) {
		PrintWriter out = null;
		BufferedReader in = null;
		String result = "";
		try {
			URL realUrl = new URL(url);
			// 打开和URL之间的连接
			URLConnection conn = realUrl.openConnection();
			// 设置通用的请求属性
			conn.setRequestProperty("accept", "*/*");
			conn.setRequestProperty("connection", "Keep-Alive");
			conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
			// 发送POST请求必须设置如下两行
			conn.setDoOutput(true);
			conn.setDoInput(true);
			// 获取URLConnection对象对应的输出流
			out = new PrintWriter(conn.getOutputStream());
			// 发送请求参数
			out.print(param);
			// flush输出流的缓冲
			out.flush();
			// 定义BufferedReader输入流来读取URL的响应
			in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line;
			while ((line = in.readLine()) != null) {
				result += line;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		// 使用finally块来关闭输出流、输入流
		finally {
			try {
				if (out != null) {
					out.close();
				}
				if (in != null) {
					in.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return result;
	}

	/**
	 * Post请求发送 (String)
	 * 
	 * @param url
	 * @param body
	 *            RequestBody
	 * @param mimeType
	 *            例如 application/xml "application/x-www-form-urlencoded"
	 *            a=1&b=2&c=3
	 * @param charset
	 *            编码
	 * @param connTimeout
	 *            建立链接超时时间,毫秒.
	 * @param readTimeout
	 *            响应超时时间,毫秒.
	 * @return ResponseBody, 使用指定的字符集编码.
	 * @throws ConnectTimeoutException
	 *             建立链接超时异常
	 * @throws SocketTimeoutException
	 *             响应超时
	 * @throws Exception
	 */
	public static String post(String url, String body, String mimeType, String charset, Integer connTimeout, Integer readTimeout)
			throws ConnectTimeoutException, SocketTimeoutException, Exception {
		HttpClient client = null;
		HttpPost post = new HttpPost(url);
		String result = "";
		try {
			if (StringUtils.isNotBlank(body)) {
				HttpEntity entity = new StringEntity(body, ContentType.create(mimeType, charset));
				post.setEntity(entity);
			}
			// 设置参数
			Builder customReqConf = RequestConfig.custom();
			if (connTimeout != null) {
				customReqConf.setConnectTimeout(connTimeout);
			}
			if (readTimeout != null) {
				customReqConf.setSocketTimeout(readTimeout);
			}
			post.setConfig(customReqConf.build());

			HttpResponse res;
			if (url.startsWith("https")) {
				// 执行 Https 请求.
				client = createSSLInsecureClient();
				res = client.execute(post);
			} else {
				// 执行 Http 请求.
				client = HttpClientUtils.client;
				res = client.execute(post);
			}
			result = IOUtils.toString(res.getEntity().getContent(), charset);
		} finally {
			post.releaseConnection();
			if (url.startsWith("https") && client != null && client instanceof CloseableHttpClient) {
				((CloseableHttpClient) client).close();
			}
		}
		return result;
	}

	/**
	 * 提交form表单 (Map)
	 * 
	 * @param url
	 * @param params
	 * @param connTimeout
	 * @param readTimeout
	 * @return
	 * @throws ConnectTimeoutException
	 * @throws SocketTimeoutException
	 * @throws Exception
	 */
	public static String postForm(String url, Map<String, String> params, Map<String, String> headers, Integer connTimeout, Integer readTimeout,
			boolean paramsBalues) throws ConnectTimeoutException, SocketTimeoutException, Exception {

		HttpClient client = null;
		HttpPost post = new HttpPost(url);
		try {
			if (params != null && !params.isEmpty()) {
				if (paramsBalues) {
					List<NameValuePair> formParams = new ArrayList<org.apache.http.NameValuePair>();
					Set<Entry<String, String>> entrySet = params.entrySet();
					for (Entry<String, String> entry : entrySet) {
						formParams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
					}
					UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formParams, URL_PARAM_DECODECHARSET_UTF8);
					post.setEntity(entity);
				} else {
					StringEntity entity = new StringEntity(JSONObject.fromObject(params).toString(), "utf-8");
					post.setEntity(entity);
				}
			}

			if (headers != null && !headers.isEmpty()) {
				for (Entry<String, String> entry : headers.entrySet()) {
					post.addHeader(entry.getKey(), entry.getValue());
				}
			}
			// 设置参数
			Builder customReqConf = RequestConfig.custom();
			if (connTimeout != null) {
				customReqConf.setConnectTimeout(connTimeout);
			}
			if (readTimeout != null) {
				customReqConf.setSocketTimeout(readTimeout);
			}
			post.setConfig(customReqConf.build());
			HttpResponse res = null;
			if (url.startsWith("https")) {
				// 执行 Https 请求.
				client = createSSLInsecureClient();
				res = client.execute(post);
			} else {
				// 执行 Http 请求.
				client = HttpClientUtils.client;
				res = client.execute(post);
			}
			return IOUtils.toString(res.getEntity().getContent(), URL_PARAM_DECODECHARSET_UTF8);
		} finally {
			post.releaseConnection();
			if (url.startsWith("https") && client != null && client instanceof CloseableHttpClient) {
				((CloseableHttpClient) client).close();
			}
		}
	}

	/**
	 * 提交form表单 (JSON)
	 * 
	 * @param url
	 * @param jsonString
	 * @param connTimeout
	 * @param readTimeout
	 * @return
	 * @throws ConnectTimeoutException
	 * @throws SocketTimeoutException
	 * @throws Exception
	 */
	public static String postForm(String url, String jsonString, Integer connTimeout, Integer readTimeout) throws ConnectTimeoutException,
			SocketTimeoutException, Exception {

		HttpClient client = null;
		HttpPost post = new HttpPost(url);
		try {
			if (jsonString != null && !jsonString.isEmpty()) {
				post.addHeader(HTTP.CONTENT_TYPE, CONTENT_TYPE_TEXT_JSON);
				StringEntity se = new StringEntity(jsonString, URL_PARAM_DECODECHARSET_UTF8);
				se.setContentType(CONTENT_TYPE_TEXT_JSON);
				se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, CONTENT_TYPE_TEXT_JSON));
				post.setEntity(se);
			}

			// 设置参数
			Builder customReqConf = RequestConfig.custom();
			if (connTimeout != null) {
				customReqConf.setConnectTimeout(connTimeout);
			}
			if (readTimeout != null) {
				customReqConf.setSocketTimeout(readTimeout);
			}
			post.setConfig(customReqConf.build());
			HttpResponse res = null;
			if (url.startsWith("https")) {
				// 执行 Https 请求.
				client = createSSLInsecureClient();
				res = client.execute(post);
			} else {
				// 执行 Http 请求.
				client = HttpClientUtils.client;
				res = client.execute(post);
			}
			return IOUtils.toString(res.getEntity().getContent(), URL_PARAM_DECODECHARSET_UTF8);
		} finally {
			post.releaseConnection();
			if (url.startsWith("https") && client != null && client instanceof CloseableHttpClient) {
				((CloseableHttpClient) client).close();
			}
		}
	}

	/**
	 * 发送一个 GET 请求
	 * 
	 * @param url
	 * @param charset
	 * @param connTimeout
	 *            建立链接超时时间,毫秒.
	 * @param readTimeout
	 *            响应超时时间,毫秒.
	 * @throws ConnectTimeoutException
	 *             建立链接超时
	 * @throws SocketTimeoutException
	 *             响应超时
	 * @throws Exception
	 */
	public static String get(String url, String charset, Integer connTimeout, Integer readTimeout) throws ConnectTimeoutException,
			SocketTimeoutException, Exception {

		HttpClient client = null;
		HttpGet get = new HttpGet(url);
		String result = "";
		try {
			// 设置参数
			Builder customReqConf = RequestConfig.custom();
			if (connTimeout != null) {
				customReqConf.setConnectTimeout(connTimeout);
			}
			if (readTimeout != null) {
				customReqConf.setSocketTimeout(readTimeout);
			}
			get.setConfig(customReqConf.build());

			HttpResponse res = null;

			if (url.startsWith("https")) {
				// 执行 Https 请求.
				client = createSSLInsecureClient();
				res = client.execute(get);
			} else {
				// 执行 Http 请求.
				client = HttpClientUtils.client;
				res = client.execute(get);
			}

			result = IOUtils.toString(res.getEntity().getContent(), charset);
		} finally {
			get.releaseConnection();
			if (url.startsWith("https") && client != null && client instanceof CloseableHttpClient) {
				((CloseableHttpClient) client).close();
			}
		}
		return result;
	}

	/**
	 * 从 response 里获取 charset
	 * 
	 * @param ressponse
	 * @return
	 */
	private static String getCharsetFromResponse(HttpResponse ressponse) {
		// Content-Type:text/html; charset=GBK
		if (ressponse.getEntity() != null && ressponse.getEntity().getContentType() != null
				&& ressponse.getEntity().getContentType().getValue() != null) {
			String contentType = ressponse.getEntity().getContentType().getValue();
			if (contentType.contains("charset=")) {
				return contentType.substring(contentType.indexOf("charset=") + 8);
			}
		}
		return null;
	}

	/**
	 * 创建 SSL连接
	 * 
	 * @return
	 * @throws GeneralSecurityException
	 */
	private static CloseableHttpClient createSSLInsecureClient() throws GeneralSecurityException {
		try {
			SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
				public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
					return true;
				}
			}).build();

			SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext, new X509HostnameVerifier() {

				@Override
				public boolean verify(String arg0, SSLSession arg1) {
					return true;
				}

				@Override
				public void verify(String host, SSLSocket ssl) throws IOException {
				}

				@Override
				public void verify(String host, X509Certificate cert) throws SSLException {
				}

				@Override
				public void verify(String host, String[] cns, String[] subjectAlts) throws SSLException {
				}

			});

			return HttpClients.custom().setSSLSocketFactory(sslsf).build();

		} catch (GeneralSecurityException e) {
			throw e;
		}
	}

	public static JSONObject sendRequest(Map<String, String> paramMap, String reqUrl) throws Exception {
		HttpEntity reqEntity = null;
		List<NameValuePair> formParams = convertNameValuePairToList(paramMap);

		reqEntity = new UrlEncodedFormEntity(formParams, "utf-8");
		HttpPost httpPost = new HttpPost(reqUrl);
		httpPost.setEntity(reqEntity);

		String result = exctueRequest(httpPost);
		if (result == null) {
			return null;
		}

		JSONObject retJson = JSONObject.fromObject(result);
		return retJson;
	}

	private static List<NameValuePair> convertNameValuePairToList(Map<String, String> requestParams) {
		if (requestParams == null || requestParams.isEmpty()) {
			return null;
		}
		List<NameValuePair> formParams = new ArrayList<NameValuePair>();
		for (Map.Entry<String, String> entry : requestParams.entrySet()) {
			formParams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
		}
		return formParams;
	}

	public static HttpClient getClient() {
		RequestConfig.Builder requestBuilder = RequestConfig.custom();
		requestBuilder = requestBuilder.setSocketTimeout(20000);
		SSLContext sslContext = null;
		try {
			sslContext = SSLContext.getInstance("SSL");
			sslContext.init(null, new TrustManager[] { new X509TrustManager() {
				public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {

				}

				public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {

				}

				public X509Certificate[] getAcceptedIssuers() {
					return new X509Certificate[] {};
				}
			} }, new java.security.SecureRandom());
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (KeyManagementException e) {
			e.printStackTrace();
		}

		CloseableHttpClient client = HttpClientBuilder.create().setSSLSocketFactory(new SSLConnectionSocketFactory(sslContext))
				.setDefaultRequestConfig(requestBuilder.build()).setHostnameVerifier(new AllowAllHostnameVerifier()).build();
		return client;

	}

	public static String exctueRequest(HttpRequestBase request) {
		HttpResponse response = null;
		String result = null;
		try {
			response = getClient().execute(request);
			int statuscode = response.getStatusLine().getStatusCode();
			if (statuscode == HttpStatus.SC_OK) {
				HttpEntity entity = response.getEntity();
				result = EntityUtils.toString(entity, "UTF-8");
			} else {
				result = null;
			}
		} catch (Exception e) {
			throw new RuntimeException("HTTP网络执行异常:" + e.toString());
		} finally {
			releaseConnection(request);
		}
		return result;
	}

	private static void releaseConnection(HttpRequestBase request) {
		if (request != null) {
			request.releaseConnection();
		}
	}

}
