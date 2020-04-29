package com.jh.paymentgateway.util.np;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

public class HttpRequestUtils {

	/**
	 * httpPost
	 * 
	 * @param url
	 *            路径
	 * @param jsonParam
	 *            参数
	 * @return
	 */
	public static JSONObject httpPost(String url, Object obj) {
		return httpPost(url, obj, false);
	}

	/**
	 * post请求
	 * 
	 * @param url
	 *            url地址
	 * @param jsonParam
	 *            参数
	 * @param noNeedResponse
	 *            不需要返回结果
	 * @return
	 */
	public static JSONObject httpPost(String url, Object obj, boolean noNeedResponse) {
		// post请求返回结果
		CloseableHttpClient httpclient = HttpClients.createDefault();
		JSONObject jsonResult = null;
		HttpPost method = new HttpPost(url);
		try {
			if (null != obj) {
				// 解决中文乱码问题
				StringEntity entity = new StringEntity(JSON.toJSONString(obj), "utf-8");
				// System.out.println(JSON.toJSONString(obj));
				entity.setContentEncoding("UTF-8");
				entity.setContentType("application/json");
				method.setEntity(entity);
			}
			CloseableHttpResponse result = httpclient.execute(method);
			url = URLDecoder.decode(url, "UTF-8");
			/** 请求发送成功，并得到响应 **/
			if (result.getStatusLine().getStatusCode() == 200) {
				String str = "";
				try {
					/** 读取服务器返回过来的json字符串数据 **/
					str = EntityUtils.toString(result.getEntity());
					if (noNeedResponse) {
						return null;
					}
					/** 把json字符串转换成json对象 **/
					jsonResult = JSON.parseObject(str);

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return jsonResult;
	}

	public static JSONObject httpPostStr(String url, String param) {
		// post请求返回结果
		CloseableHttpClient httpclient = HttpClients.createDefault();
		JSONObject jsonResult = null;
		HttpPost method = new HttpPost(url);
		try {
			if (null != param) {
				// 解决中文乱码问题
				StringEntity entity = new StringEntity(param, "utf-8");
				// System.out.println(JSON.toJSONString(param));
				entity.setContentEncoding("UTF-8");
				entity.setContentType("application/x-www-form-urlencoded");
				method.setEntity(entity);
			}
			CloseableHttpResponse result = httpclient.execute(method);
			url = URLDecoder.decode(url, "UTF-8");
			/** 请求发送成功，并得到响应 **/
			if (result.getStatusLine().getStatusCode() == 200) {
				String str = "";
				try {
					/** 读取服务器返回过来的json字符串数据 **/
					str = EntityUtils.toString(result.getEntity());
					/** 把json字符串转换成json对象 **/
					jsonResult = JSON.parseObject(str);

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return jsonResult;
	}

	public static String httpPostXml(String url, String xmlContent, String encode) {

		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpPost httpPost = null;
		BufferedReader reader = null;
		int i = 0;
		while (i < 1) {
			try {
				httpPost = new HttpPost(url);
				RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(90000)
						.setConnectionRequestTimeout(90000).setSocketTimeout(90000).build();
				httpPost.setConfig(requestConfig);
				StringEntity myEntity = new StringEntity(xmlContent, encode);
				httpPost.addHeader("Content-Type", "text/xml; charset=" + encode);
				httpPost.setEntity(myEntity);
				HttpResponse response = httpclient.execute(httpPost);
				HttpEntity resEntity = response.getEntity();
				if (resEntity != null) {
					reader = new BufferedReader(new InputStreamReader(resEntity.getContent(), encode));
					StringBuffer sb = new StringBuffer();
					String line = null;
					while ((line = reader.readLine()) != null) {
						sb.append(line);
						sb.append("\r\n");
					}
					return sb.toString();
				}

			} catch (Exception e) {
				i++;
				if (i == 1) {
					System.out.println(e.getMessage());
				}
				if("Read timed out".equals(e.getMessage())){
					return null;
				}
			} finally {
				if (httpPost != null) {
					httpPost.abort();
				}
				if (reader != null) {
					try {
						reader.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return null;
	}

	/**
	 * 发送get请求
	 * 
	 * @param url
	 *            路径
	 * @return
	 */
	public static JSONObject httpGet(String url) {
		// get请求返回结果
		JSONObject jsonResult = null;
		try {
			CloseableHttpClient client = HttpClients.createDefault();
			// 发送get请求
			HttpGet request = new HttpGet(url);
			HttpResponse response = client.execute(request);

			/** 请求发送成功，并得到响应 **/
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				/** 读取服务器返回过来的json字符串数据 **/
				String strResult = EntityUtils.toString(response.getEntity());
				/** 把json字符串转换成json对象 **/
				jsonResult = JSON.parseObject(strResult);
				url = URLDecoder.decode(url, "UTF-8");
			} else {
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return jsonResult;
	}

	/**
	 * 发送参数以&连接的形式
	 * 
	 * @param url
	 * @param param
	 * @return
	 */
	public static String httpPost(String url, Map<String, String> map) {
		// post请求返回结果
		CloseableHttpClient httpclient = HttpClients.createDefault();
		String rsp = null;
		List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		HttpPost method = new HttpPost(url);
		try {
			if (null != map) {
				// 解决中文乱码问题
				method.setHeader("Content-type", "application/x-www-form-urlencoded; charset=utf-8");
				method.setHeader("Accept", "text/xml;charset=utf-8");
				method.setHeader("Cache-Control", "no-cache");
				Set<Map.Entry<String, String>> entrySet = map.entrySet();
				for (Map.Entry<String, String> entry : entrySet) {
					params.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
				}
				method.setEntity(new UrlEncodedFormEntity(params, "utf-8"));
			}
			CloseableHttpResponse result = httpclient.execute(method);
			/** 请求发送成功，并得到响应 **/
			if (result.getStatusLine().getStatusCode() == 200) {
				try {
					/** 读取服务器返回过来的json字符串数据 **/
					rsp = EntityUtils.toString(result.getEntity());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return rsp;
	}

	/**
	 * http数据上下文json格式
	 * @param url
	 * @param param
	 * @return
	 * @date 2017年10月27日上午11:29:44
	 */
	public static JSONObject httpPostString(String url, String param) {
		// post请求返回结果
		CloseableHttpClient httpclient = HttpClients.createDefault();
		String httpResult=null;
		HttpPost method = new HttpPost(url);
		JSONObject jsonResult = null;
		try {
			if (null != param) {
				// 解决中文乱码问题
				StringEntity entity = new StringEntity(param, "utf-8");
				entity.setContentEncoding("UTF-8");
				entity.setContentType("application/json");
				method.setEntity(entity);
			}
			CloseableHttpResponse result = httpclient.execute(method);
			url = URLDecoder.decode(url, "UTF-8");
			/** 请求发送成功，并得到响应 **/
			if (result.getStatusLine().getStatusCode() == 200) {
				try {
					/** 读取服务器返回过来的json字符串数据 **/
					httpResult = EntityUtils.toString(result.getEntity());
					jsonResult = JSON.parseObject(httpResult);
				} catch (Exception e) {
				}
			}
		} catch (IOException e) {
		}
		return jsonResult;
	}
}
