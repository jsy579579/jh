package com.jh.paymentchannel.business.impl;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class JuHeAPIRealnameAuthService {

	public static String realNameAuth(String idcard, String realname,String realNameUrl,String key){
		realNameUrl =  realNameUrl + "?idcard="+idcard+"&realname="+realname+"&key="+key;
//		 url =  "http://op.juhe.cn/idcard/query?idcard="+idcard+"&realname="+realname+"&key=2a7fc8c8e5c4d6026d1df59396ecbaa6";
		 RestTemplate restTemplate=new RestTemplate();
		 ResponseEntity<String> resultStr = restTemplate.exchange(realNameUrl,HttpMethod.GET, null, String.class);
		 String responseCode = resultStr.getBody();
		 return responseCode;
		 
	}
	
	
	
	
	
}
