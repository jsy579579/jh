package com.jh.paymentgateway.util.hxdh;


import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HttpClientUtils {

	public static String doGet(String url, Map<String, String> Headers, Map<String, String> param) {

		// 创建Httpclient对象
		CloseableHttpClient httpclient = HttpClients.createDefault();

		String result = "";
		CloseableHttpResponse response = null;
		HttpGet httpGet = new HttpGet();
		try {
			if(null != param){
				// 创建uri
				URIBuilder builder = new URIBuilder(url);
				if (param != null) {
					for (String key : param.keySet()) {
						builder.addParameter(key, param.get(key));
					}
				}
				URI uri = builder.build();
				httpGet.setURI(uri);
			}

			if(null == param){
				// 创建uri
				URIBuilder builder = new URIBuilder(url);
				URI uri = builder.build();
				// 创建http GET请求
				httpGet.setURI(uri);
			}
			if (Headers != null) {
				for (String key : Headers.keySet()) {
					httpGet.setHeader(key,Headers.get(key));
				}
			}

			// 执行请求
			response = httpclient.execute(httpGet);
			if (response.getStatusLine().getStatusCode() == 200) {
				result = EntityUtils.toString(response.getEntity(), "UTF-8");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (response != null) {
					response.close();
				}
				httpclient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}


	public static String doGet(String url,Map<String, String> Headers) {
		return doGet(url, Headers,null);
	}


	public static String doPost(String url,Map<String,String>Headers,Map<String,String> bodyMap) {
		return doPost(url,Headers,bodyMap,null,null);
	}
	public static String doPost(String url,Map<String,String>Headers,Map<String,String> bodyMap,File file) {
		return doPost(url,Headers,bodyMap,file,null);
	}
	public static String doPost(String url,Map<String,String>Headers,String json) {
		return doPost(url,Headers,null,null,json);
	}


	private static String doPost(String url, Map<String,String>Headers, Map<String,String> bodyMap, File file,String json) {
		// 创建Httpclient对象
		CloseableHttpClient httpClient = HttpClients.createDefault();
		CloseableHttpResponse response = null;
		String result = "";
		try {
			// 创建Http Post请求
			HttpPost httpPost = new HttpPost(url);
			if (Headers != null) {
				for (String key : Headers.keySet()) {
					httpPost.setHeader(key,Headers.get(key));
				}
			}
			// 创建请求内容
			if(null != file && null != bodyMap){
				MultipartEntityBuilder entity = MultipartEntityBuilder
                        .create()
                        .addBinaryBody("file",file);
				for (String key : bodyMap.keySet()) {
					entity.addPart(key,new StringBody(bodyMap.get(key),Charset.forName("utf-8")));
				}
				HttpEntity requestEntity = entity.build();
				httpPost.setEntity(requestEntity);
			}

			if(null != bodyMap && null == file && StringUtils.isBlank(json)){
				List<NameValuePair> paramList = new ArrayList<>();
				for (String key : bodyMap.keySet()) {
					paramList.add(new BasicNameValuePair(key, bodyMap.get(key)));
				}
				UrlEncodedFormEntity entity = new UrlEncodedFormEntity(paramList, "utf-8");
				httpPost.setEntity(entity);
			}

			if(StringUtils.isNotBlank(json)){
				StringEntity entity = new StringEntity(json, ContentType.APPLICATION_JSON);
				httpPost.setEntity(entity);
			}
			// 执行http请求
			response = httpClient.execute(httpPost);
			result = EntityUtils.toString(response.getEntity(), "utf-8");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				response.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return result;
	}
}
