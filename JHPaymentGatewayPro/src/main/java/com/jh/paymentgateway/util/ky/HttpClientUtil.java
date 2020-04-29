package com.jh.paymentgateway.util.ky;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by xt on 2016/11/21.
 */
public class HttpClientUtil {
	//超时时间
	private static final int timeOut = 50000;

	//最大连接数
	private static final int maxTotal = 1000;

	//基础连接数
	private static final int maxPerRoute = 40;

	//目标主机最大连接数
	private static final int maxRoute = 300;

	private static CloseableHttpClient httpClient = null;

	private final static Object syncLock = new Object();

	private static void config(HttpRequestBase httpRequestBase) {
		// 设置Header等
		//         httpRequestBase.setHeader("User-Agent", "Mozilla/5.0");
		//         httpRequestBase
		//         .setHeader("Accept",
		//         "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		//         httpRequestBase.setHeader("Accept-Language",
		//         "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");// "en-US,en;q=0.5");
		//         httpRequestBase.setHeader("Accept-Charset",
		//         "ISO-8859-1,utf-8,gbk,gb2312;q=0.7,*;q=0.7");

		// 配置请求的超时设置
		RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(timeOut)
				.setConnectTimeout(timeOut).setSocketTimeout(timeOut).build();
		httpRequestBase.setConfig(requestConfig);
	}

	/**
	 * 获取HttpClient对象
	 */
	public static CloseableHttpClient getHttpClient(String url) {
		String hostname = url.split("/")[2];
		int port = 80;
		if (hostname.contains(":")) {
			String[] arr = hostname.split(":");
			hostname = arr[0];
			port = Integer.parseInt(arr[1]);
		}
		if (httpClient == null) {
			synchronized (syncLock) {
				if (httpClient == null) {
					httpClient = createHttpClient(hostname, port);
				}
			}
		}
		return httpClient;
	}

	/**
	 * 创建HttpClient对象
	 */
	public static CloseableHttpClient createHttpClient(String hostname, int port) {
		SSLContext sslContext = null;
		try {
			sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
				// 信任所有  
				public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
					return true;
				}
			}).build();
		} catch (Exception e) {
			e.printStackTrace();
		}

		ConnectionSocketFactory plainsf = PlainConnectionSocketFactory.getSocketFactory();
		LayeredConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext, new String[] { "TLSv1" },
				null, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
		Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory> create()
				.register("http", plainsf).register("https", sslsf).build();
		PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(registry);
		// 将最大连接数增加
		cm.setMaxTotal(maxTotal);
		// 将每个路由基础的连接增加
		cm.setDefaultMaxPerRoute(maxPerRoute);
		HttpHost httpHost = new HttpHost(hostname, port);
		// 将目标主机的最大连接数增加
		cm.setMaxPerRoute(new HttpRoute(httpHost), maxRoute);

		CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(cm).build();

		return httpClient;
	}

	private static void setPostParams(HttpPost httpost, Map<String, String> params) {
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		Set<String> keySet = params.keySet();
		for (String key : keySet) {
			nvps.add(new BasicNameValuePair(key, params.get(key)));
		}
		try {
			httpost.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	/**
	 * POST方式发送json数据
	 *
	 * @param parameters
	 * @return
	 */
	public static String postJson(String url, String parameters) throws Exception {
		HttpPost httppost = new HttpPost(url);
		config(httppost);
		// 建立一个NameValuePair数组，用于存储欲传送的参数
		httppost.addHeader("Content-type", "application/json; charset=utf-8");
		httppost.setHeader("Accept", "application/json");
		httppost.setEntity(new StringEntity(parameters, Charset.forName("UTF-8")));
		CloseableHttpResponse response = null;
		try {
			response = getHttpClient(url).execute(httppost, HttpClientContext.create());
			String result = EntityUtils.toString(response.getEntity());
			return result;
		} catch (Exception e) {
			throw e;
		} finally {
			try {
				if (response != null)
					response.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * POST方式发送json
	 *
	 * @param parameters
	 * @return
	 */
	public static String postJson2(String url, String parameters) throws Exception {
		HttpPost httppost = new HttpPost(url);
		config(httppost);
		// 建立一个NameValuePair数组，用于存储欲传送的参数
		httppost.addHeader("Content-type", "text/plain;charset=UTF-8");
		httppost.setEntity(new StringEntity(parameters, Charset.forName("utf-8")));

		CloseableHttpResponse response = null;
		try {
			response = getHttpClient(url).execute(httppost, HttpClientContext.create());
			String result = EntityUtils.toString(response.getEntity(), "utf-8");
			return result;
		} catch (Exception e) {
			throw e;
		} finally {
			try {
				if (response != null)
					response.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * POST请求URL获取内容
	 */
	public static String post(String url, Map params) throws IOException {
		HttpPost httppost = new HttpPost(url);
		config(httppost);
		setPostParams(httppost, params);
		CloseableHttpResponse response = null;
		try {
			response = getHttpClient(url).execute(httppost, HttpClientContext.create());
			HttpEntity entity = response.getEntity();
			String result = EntityUtils.toString(entity, "utf-8");
			EntityUtils.consume(entity);
			return result;
		} catch (Exception e) {
			throw e;
		} finally {
			try {
				if (response != null)
					response.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}


	/**
	 * post请求发送xml报文
	 *
	 * @param reqData
	 * @param url
	 * @return
	 */
	public static String post(String reqData, String url) throws IOException {
		HttpPost httpPost = new HttpPost(url);
		config(httpPost);
		CloseableHttpResponse response = null;
		try {
			StringEntity entityParams = new StringEntity(reqData, "utf-8");
			httpPost.setEntity(entityParams);
			httpPost.setHeader("Content-Type", "text/xml;charset=" + "utf-8");
			response = getHttpClient(url).execute(httpPost, HttpClientContext.create());
			HttpEntity entity = response.getEntity();
			String result = EntityUtils.toString(entity, "utf-8");
			EntityUtils.consume(entity);
			return result;
		} catch (Exception e) {
			throw e;
		} finally {
			try {
				if (response != null)
					response.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * GET请求URL获取内容
	 */
	public static String get(String url) {
		HttpGet httpget = new HttpGet(url);
		config(httpget);
		CloseableHttpResponse response = null;
		try {
			response = getHttpClient(url).execute(httpget, HttpClientContext.create());
			HttpEntity entity = response.getEntity();
			String result = EntityUtils.toString(entity, "utf-8");
			EntityUtils.consume(entity);
			return result;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (response != null)
					response.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

}
