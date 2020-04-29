package com.jh.paymentchannel.business.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import net.sf.json.JSONObject;

public class JuHeAPIBankCard4AuthService {

	private static final Logger log = LoggerFactory.getLogger(BankCard4AuthBusinessImpl.class);
	
	public static String  bankCard4AuthJuhe(String mobile, String bankCard, String idcard, String realname,String auth4Url,String key){
		 auth4Url = auth4Url + "?bankcard="+bankCard+"&idcard="+idcard+"&mobile="+mobile+"&realname="+realname+"&key="+key;
//		 String url = "http://v.juhe.cn/verifybankcard4/query?bankcard="+bankCard+"&idcard="+idcard+"&mobile="+mobile+"&realname="+realname+"&key=f0add8418c505eda3f7a1e6865135f9e";
		 RestTemplate restTemplate=new RestTemplate();
		 ResponseEntity<String> resultStr = restTemplate.exchange(auth4Url,HttpMethod.GET, null, String.class);
		 String responseCode = resultStr.getBody();
		 return responseCode;
	}
	
	public  static String  bankCard4AuthAli(String mobile, String bankCard, String idcard, String realname) {
	    String host = "http://jisubank4.market.alicloudapi.com";
	    String path = "/bankcardverify4/verify";
	    String method = "GET";
	    String appcode = "a5d1104a6f73467f85a372562ea69d55";
	    Map<String, String> headers = new HashMap<String, String>();
	    //最后在header中的格式(中间是英文空格)为Authorization:APPCODE 83359fd73fe94948385f570e3c139105
	    headers.put("Authorization", "APPCODE " + appcode);
	    Map<String, String> querys = new HashMap<String, String>();
	    querys.put("bankcard", bankCard);
	    querys.put("idcard", idcard.trim());
	    querys.put("mobile", mobile.trim());
	    querys.put("realname", realname.trim());


	    try {
	    	/**
	    	* 重要提示如下:
	    	* HttpUtils请从
	    	* https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/src/main/java/com/aliyun/api/gateway/demo/util/HttpUtils.java
	    	* 下载
	    	*
	    	* 相应的依赖请参照
	    	* https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/pom.xml
	    	*/
	    	log.info("阿里云数据四要素查询开始"+headers);
	    	HttpResponse response = HttpUtils.doGet(host, path, method, headers, querys);
	    	log.info("阿里云数据四要素查询结果"+response.toString());
	    	String josn=EntityUtils.toString(response.getEntity());
	    	return josn;
	    	//获取response的body
	    	
	    } catch (Exception e) {
	    	log.error("",e);
	    	return "";
		}
	}
//	public static void main(String[] args) {
//	    String host = "http://jisubank4.market.alicloudapi.com";
//	    String path = "/bankcardverify4/verify";
//	    String method = "GET";
//	    String appcode = "a5d1104a6f73467f85a372562ea69d55";
//	    Map<String, String> headers = new HashMap<String, String>();
//	    //最后在header中的格式(中间是英文空格)为Authorization:APPCODE 83359fd73fe94948385f570e3c139105
//	    headers.put("Authorization", "APPCODE " + appcode);
//	    Map<String, String> querys = new HashMap<String, String>();
//	    querys.put("bankcard", "6228480841754115613");
//	    querys.put("idcard", "452501197210040715");
//	    querys.put("mobile", "15977942569");
//	    querys.put("realname", "罗勇");
//
//
//	    try {
//	    	/**
//	    	* 重要提示如下:
//	    	* HttpUtils请从
//	    	* https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/src/main/java/com/aliyun/api/gateway/demo/util/HttpUtils.java
//	    	* 下载
//	    	*
//	    	* 相应的依赖请参照
//	    	* https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/pom.xml
//	    	*/
//	    	HttpResponse response = HttpUtils.doGet(host, path, method, headers, querys);
//	    	System.out.println(response.toString());
//	    	//获取response的body
//	    	System.out.println(EntityUtils.toString(response.getEntity()));
//	    } catch (Exception e) {
//	    	e.printStackTrace();
//	    }
//	}
	
	public static String bankCardLocation(String bankCard,String cardLocationUrl,String key){
		cardLocationUrl = cardLocationUrl + "?bankcard="+bankCard+"&key=" + key;
//		String url = "http://v.juhe.cn/bankcardinfo/query?bankcard="+bankCard+"&key=e6db2dc1255e580f5e001f62d5f00613";
	 	RestTemplate restTemplate=new RestTemplate();
		ResponseEntity<String> resultStr = restTemplate.exchange(cardLocationUrl,HttpMethod.GET, null, String.class);
		String responseCode = resultStr.getBody();
		return responseCode;
		
	}
	
}
