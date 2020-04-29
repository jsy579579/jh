package com.jh.paymentgateway.util.np;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
 
 
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jh.paymentgateway.controller.NPpageRequest;
/** 
 * 发起http请求并获取结果  
 * @author zyz 
 * @date 20140522 
 * 
 */  
public class HttpRequestUtil {  
	private static final Logger log = LoggerFactory.getLogger(NPpageRequest.class);
	 
	//post请求方法
	  public static String sendPost(String url, String data) {
	    String response = null;
	    log.info("url: " + url);
	    log.info("request: " + data);
	    try {
	      CloseableHttpClient httpclient = null;
	      CloseableHttpResponse httpresponse = null;
	      try {
	        httpclient = HttpClients.createDefault();
	        HttpPost httppost = new HttpPost(url);
	        StringEntity stringentity = new StringEntity(data,
	            ContentType.create("text/json", "UTF-8"));
	        httppost.setEntity(stringentity);
	        httpresponse = httpclient.execute(httppost);
	        response = EntityUtils
	            .toString(httpresponse.getEntity());
	        log.info("response: " + response);
	      } finally {
	        if (httpclient != null) {
	          httpclient.close();
	        }
	        if (httpresponse != null) {
	          httpresponse.close();
	        }
	      }
	    } catch (Exception e) {
	      e.printStackTrace();
	    }
	    return response;
	  }
  
} 