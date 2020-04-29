package com.jh.paymentgateway.util.xk;


import org.apache.http.HttpEntity;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


public class HttpClientUtil {
	
	/**
	 * 模拟form表单提交参数
	 * @param url 
	 * @param map
	 * @param charset
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public String doPost(String url, Map<String, String> map, String charset) {
		CloseableHttpClient httpClient = null;
		HttpPost httpPost = null;
		String result = null;
		CloseableHttpResponse response = null;
		try {
			if (url.contains("https")) {
				httpClient = SSLClient.creatSSLClient();
			}else{
				httpClient = HttpClients.createDefault();
			}
			httpPost = new HttpPost(url);
			// 设置参数
			List<BasicNameValuePair> list = new ArrayList<>();
			Iterator iterator = map.entrySet().iterator();
			while (iterator.hasNext()) {
				Entry<String, String> elem = (Entry<String, String>) iterator
						.next();
				list.add(new BasicNameValuePair(elem.getKey(), elem.getValue()));
			}
			if (list.size() > 0) {
				UrlEncodedFormEntity entity = new UrlEncodedFormEntity(list,
						charset);
				httpPost.setEntity(entity);
			}
			
			//设置连接超时时间
			RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(3000).build();
			httpPost.setConfig(requestConfig);
			
			response = httpClient.execute(httpPost);
			if (response != null) {
				HttpEntity resEntity = response.getEntity();
				if (resEntity != null) {
					result = EntityUtils.toString(resEntity, charset);
					EntityUtils.consume(resEntity);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			try {
				if(response != null)
					response.close();
				if(httpPost != null)
					httpPost.releaseConnection();
				if(httpClient != null)
					httpClient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	
	/**
	 * 提交json格式参数
	 * @param url
	 * @param param json格式字符串
	 * @param charset
	 * @return
	 */
	public String doPost(String url, String param, String charset) {
		CloseableHttpClient httpClient = null;
		HttpPost httpPost = null;
		String result = null;
		CloseableHttpResponse response = null;
		try {
			if (url.contains("https")) {
				httpClient = SSLClient.creatSSLClient();
			}else{
				httpClient = HttpClients.createDefault();
			}
			httpPost = new HttpPost(url);
			// 设置参数
			StringEntity entity = new StringEntity(param, charset);
        	entity.setContentEncoding(charset);
        	entity.setContentType("application/json");
        	httpPost.setEntity(entity); 
        	httpPost.setHeader("Content-Type", "application/json;charset=UTF-8");
        	
        	//设置连接超时时间
			RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(3000).build();
			httpPost.setConfig(requestConfig);
        	
			response = httpClient.execute(httpPost);
			if (response != null) {
				HttpEntity resEntity = response.getEntity();
				if (resEntity != null) {
					result = EntityUtils.toString(resEntity, charset);
					EntityUtils.consume(resEntity);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			try {
				if(response != null)
					response.close();
				if(httpPost != null)
					httpPost.releaseConnection();
				if(httpClient != null)
					httpClient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public String doGet(String url, Map<String, String> map, String charset) {
		CloseableHttpClient httpClient = null;
		HttpGet httpGet = null;
		String result = null;
		CloseableHttpResponse response = null;
		try {
			if (url.contains("https")) {
				httpClient = SSLClient.creatSSLClient();
			}else{
				httpClient = HttpClients.createDefault();
			}
			
			// 设置参数
			List<BasicNameValuePair> list = new ArrayList<>();
			Iterator iterator = map.entrySet().iterator();
			while (iterator.hasNext()) {
				Entry<String, String> elem = (Entry<String, String>) iterator
						.next();
				list.add(new BasicNameValuePair(elem.getKey(), elem.getValue()));
			}
			if (list.size() > 0) {
				UrlEncodedFormEntity entity = new UrlEncodedFormEntity(list,charset);
				url = url + "?" + EntityUtils.toString(entity);
			}
			
			httpGet = new HttpGet(url);
			
			//设置连接超时时间
			RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(3000).build();
			httpGet.setConfig(requestConfig);
			
			response = httpClient.execute(httpGet);
			
			if (response != null) {
				HttpEntity resEntity = response.getEntity();
				if (resEntity != null) {
					result = EntityUtils.toString(resEntity, charset);
					EntityUtils.consume(resEntity);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			try {
				if(response != null)
					response.close();
				if(httpGet != null)
					httpGet.releaseConnection();
				if(httpClient != null)
					httpClient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	
//	/**
//	 * 上传文件
//	 * @param url
//	 * @param file
//	 * @param charset
//	 * @return
//	 */
//	public String upload(String url, File file, String charset) {
//		CloseableHttpClient httpClient = null;
//		HttpPost httpPost = null;
//		String result = null;
//		CloseableHttpResponse response = null;
//		try {
//			if (url.contains("https")) {
//				httpClient = SSLClient.creatSSLClient();
//			}else{
//				httpClient = HttpClients.createDefault();
//			}
//
//			httpPost = new HttpPost(url);
//
//			//设置连接超时时间
//			RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(3000).build();
//			httpPost.setConfig(requestConfig);
//
//			MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
//			entityBuilder.addBinaryBody(file.getName(), file);
//			HttpEntity httpEntity = entityBuilder.build();
////			FileBody fileBody = new FileBody(file);
////			HttpEntity httpEntity = MultipartEntityBuilder.create().addPart(file.getName(),fileBody).build();
//			httpPost.setEntity(httpEntity);
//
//			response = httpClient.execute(httpPost);
//
//			if (response != null) {
//				HttpEntity resEntity = response.getEntity();
//				if (resEntity != null) {
//					result = EntityUtils.toString(resEntity, charset);
//					EntityUtils.consume(resEntity);
//				}
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}finally {
//			try {
//				if(response != null)
//					response.close();
//				if(httpPost != null)
//					httpPost.releaseConnection();
//				if(httpClient != null)
//					httpClient.close();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
//		return result;
//	}
	
}
