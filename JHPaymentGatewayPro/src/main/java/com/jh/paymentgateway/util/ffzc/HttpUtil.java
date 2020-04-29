package com.jh.paymentgateway.util.ffzc;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.Consts;
import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import com.alibaba.fastjson.JSONObject;

public class HttpUtil {

	public static String CONTENT_TYPE_JSON = "application/json";
	public static String CONTENT_TYPE_XML = "text/xml";
	public static String CONTENT_TYPE_FROM = "application/x-www-form-urlencoded";
	public static String CONTENT_TYPE_FILE = "multipart/form-data";

	public static String sendPost(String reqURL, String data, String contentType) {
		CloseableHttpClient httpClient = HttpClients.createDefault();
		HttpPost method = new HttpPost(reqURL);
		RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(90000).setConnectTimeout(30000).build();// 设置请求和传输超时时间
		method.setConfig(requestConfig);
		StringEntity entity = new StringEntity(data, "UTF-8");//
		entity.setContentEncoding("UTF-8");
		entity.setContentType(contentType);
		method.setEntity(entity);
		try {
			HttpResponse resp = httpClient.execute(method);
			HttpEntity HttpResp = resp.getEntity();
			String respData = EntityUtils.toString(HttpResp);
			return respData;
		} catch (Exception e) {
			return null;
		} finally {
			method.releaseConnection();
		}
	}

	/**
	 * 
	 * @param reqURL
	 *            请求参数应该是 name1=value1&name2=value2 的形式
	 * @return
	 */
	public static String sendGet(String reqURL) {
		try {
			CloseableHttpClient httpClient = HttpClients.createDefault();
			HttpGet httpGet = new HttpGet(reqURL);
			HttpResponse resp = httpClient.execute(httpGet);
			String respData = EntityUtils.toString(resp.getEntity());
			return respData;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private static ConnectionKeepAliveStrategy myStrategy = null;
	static {
		myStrategy = new ConnectionKeepAliveStrategy() {
			public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
				HeaderElementIterator it = new BasicHeaderElementIterator(response.headerIterator("Keep-Alive"));
				while (it.hasNext()) {
					HeaderElement he = it.nextElement();
					String param = he.getName();
					String value = he.getValue();
					if ((value != null) && (param.equalsIgnoreCase("timeout"))) {
						return Long.parseLong(value) * 1000L;
					}
				}
				return 5000L;
			}
		};
	}

	private static CloseableHttpClient httpclient = HttpClientBuilder.create().setMaxConnTotal(90)
			.setMaxConnPerRoute(15).setKeepAliveStrategy(myStrategy).build();

	public static JSONObject post(Map<String, String> params, String url) throws Exception {
		HttpPost post = new HttpPost(url);

		List<NameValuePair> list = new ArrayList<NameValuePair>();

		Iterator<String> it = params.keySet().iterator();
		while (it.hasNext()) {
			String key = it.next();
			list.add(new BasicNameValuePair(key, (String) params.get(key)));
		}
		post.setEntity(new UrlEncodedFormEntity(list, Consts.UTF_8));
		CloseableHttpResponse response = httpclient.execute(post);
		JSONObject jo = null;
		if (response.getStatusLine().getStatusCode() == 200) {
			HttpEntity entity = response.getEntity();
			String str = EntityUtils.toString(entity);
			if (entity != null)
				jo = JSONObject.parseObject(str);
		} else {
			EntityUtils.consume(response.getEntity());
		}

		return jo;
	}

	public static String postAndReturnString(Map<String, String> params, String url) throws Exception {
		HttpPost post = new HttpPost(url);

		List<NameValuePair> list = new ArrayList<NameValuePair>();

		Iterator<String> it = params.keySet().iterator();
		while (it.hasNext()) {
			String key = it.next();
			list.add(new BasicNameValuePair(key, (String) params.get(key)));
		}
		post.setEntity(new UrlEncodedFormEntity(list, Consts.UTF_8));
		CloseableHttpResponse response = httpclient.execute(post);
		String jo = null;
		if (response.getStatusLine().getStatusCode() == 200) {
			HttpEntity entity = response.getEntity();
			jo = EntityUtils.toString(entity);
		} else {
			EntityUtils.consume(response.getEntity());
		}

		return jo;
	}

	public static String testPost(Map<String, String> params, String url) throws Exception {
		HttpPost post = new HttpPost(url);
		post.addHeader("User-Agent",
				"Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1; WOW64; Trident/4.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; InfoPath.2; .NET4.0C; .NET4.0E)");
		List<NameValuePair> list = new ArrayList<NameValuePair>();

		Iterator<String> it = params.keySet().iterator();
		while (it.hasNext()) {
			String key = it.next();
			list.add(new BasicNameValuePair(key, (String) params.get(key)));
		}
		post.setEntity(new UrlEncodedFormEntity(list, Consts.UTF_8));
		CloseableHttpResponse response = httpclient.execute(post);

		String redirectUrl = null;
		if (response.getStatusLine().getStatusCode() == 302) {
			redirectUrl = response.getFirstHeader("Location").getValue();
		} else if (response.getStatusLine().getStatusCode() == 200) {
			HttpEntity entity = response.getEntity();
			redirectUrl = EntityUtils.toString(entity);
		}

		return redirectUrl;

	}

	public static String writeStringEntity(String content, String reqUrl) {

		HttpPost method = new HttpPost(reqUrl);
		String entity = "响应";
		try {
			method.setEntity(new StringEntity(content, ContentType.create("text/xml", "utf-8")));
			CloseableHttpResponse r = httpclient.execute(method);
			if (r.getStatusLine().getStatusCode() == 200) {
				entity = EntityUtils.toString(r.getEntity());
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			method.releaseConnection();
		}
		return entity;

	}

	public static String writeStringJsonEntity(String content, String reqUrl) {

		HttpPost method = new HttpPost(reqUrl);
		String entity = "响应";
		try {
			method.setEntity(new StringEntity(content, ContentType.create("application/json", "utf-8")));
			CloseableHttpResponse r = httpclient.execute(method);
			if (r.getStatusLine().getStatusCode() == 200) {
				entity = EntityUtils.toString(r.getEntity());
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			method.releaseConnection();
		}
		return entity;

	}

	public static String testGet(String url) throws Exception {
		HttpGet post = new HttpGet(url);

		CloseableHttpResponse response = httpclient.execute(post);
		HttpEntity entity = response.getEntity();

		String str = EntityUtils.toString(entity);
		return str;

	}

	public static String write(String content, String reqUrl) {
		try {
			// SSLContext sslContext = SSLContext.getInstance("SSL");
			// sslContext.init(new KeyManager[0],
			// new TrustManager[] { trustManager }, new SecureRandom());
			// SSLContext.setDefault(sslContext);

			URL url = new URL(reqUrl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setConnectTimeout(10000);
			conn.setReadTimeout(10000);
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setRequestProperty("Content-Type", "application/json;charset=utf-8");

			conn.connect();
			if (content != null) {
				OutputStream out = conn.getOutputStream();
				out.write(content.getBytes("utf-8"));
			}
			InputStream in = conn.getInputStream();
			byte[] data = new byte[1024];
			StringBuffer resp = new StringBuffer("");
			while (in.read(data) != -1) {
				resp.append(new String(data, "utf-8"));
			}

			in.close();
			conn.disconnect();
			return resp.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
