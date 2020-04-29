package com.jh.paymentgateway.util.ryt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpHost;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author shuai httpClient 连接工具类
 */
public class HttpClientUtil {

	private static Logger logger = LoggerFactory.getLogger(HttpClientUtil.class);

	public static String sendPost(String url, String json) {
		logger.info("http连接：url=" + url + ",参数=" + json);
		CloseableHttpClient client = HttpClients.createDefault();
		try {
			// 1.3 参数配置3：编码格式
			String charset = "UTF-8";
			// 2. 获取请求目标，创建请求对象和响应对象
			URI uri = null;
			try {
				uri = new URI(url);
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
			int port = uri.getPort();
			if (port == -1) {
				port = 80;// 协议默认端口
			}
			// 2.1 获取请求目标
			HttpHost target = new HttpHost(uri.getHost(), port, "http");
			// 2.2 创建请求对象
			HttpPost request = new HttpPost(uri);
			// 2.3 创建响应对象
			CloseableHttpResponse response = null;
			try {
				// 3. 封装请求参数，设置配置信息
				StringEntity se = new StringEntity(json, charset);
				request.setEntity(se);
				se.setContentType("application/json");
				// 4. 发送post请求
				response = client.execute(target, request);
				// 5. 读取响应信息
				BufferedReader reader = null;
				try {
					reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
					StringBuilder result = new StringBuilder();
					String message = null;
					while ((message = reader.readLine()) != null) {
						result.append(message).append(System.getProperty("line.separator"));
					}
					String resultStr = result.toString();
					logger.info("http连接返回:"+resultStr);
					resultStr = resultStr.trim();// 去掉前后空格
					// 移除制表位换行符等，非必要
					resultStr = resultStr.replace("\n", "");
					resultStr = resultStr.replace("\r", "");
					resultStr = resultStr.replace("\t", "");
					return resultStr;
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				} catch (IllegalStateException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					try {
						if (reader != null) {
							reader.close();
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (response != null) {
					try {
						response.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		} finally {
			// 释放资源
			try {
				client.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
}
