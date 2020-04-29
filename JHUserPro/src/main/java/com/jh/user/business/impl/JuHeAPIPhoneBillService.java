package com.jh.user.business.impl;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.jh.user.util.MD5Util;


public class JuHeAPIPhoneBillService {

	/**
	 * 手机号产品查询
	 * **/
	public static String mobileTelquery(String phone, String cardnum,String key){
		
		 String url =  "http://op.juhe.cn/ofpay/mobile/telquery?cardnum="+cardnum+"&phoneno="+phone+"&key="+key;
		
		 RestTemplate restTemplate=new RestTemplate();
		 ResponseEntity<String> resultStr = restTemplate.exchange(url,HttpMethod.GET, null, String.class);
		 String responseCode = resultStr.getBody();
		 return responseCode;
		 
	}
	/**
	 * 手机充值接口
	 * */
	public static String mobileOnlineorder(String phone, String cardnum, String ordercode ,String key ,String OpenID){
		
		String sign=MD5Util.strToMD5(OpenID+key+phone+cardnum+ordercode);
		
		 String url =  "http://op.juhe.cn/ofpay/mobile/onlineorder?cardnum="+cardnum+"&phoneno="+phone+"&key="+key+"&orderid="+ordercode+"&sign="+sign;
		
		 RestTemplate restTemplate=new RestTemplate();
		 ResponseEntity<String> resultStr = restTemplate.exchange(url,HttpMethod.GET, null, String.class);
		 String responseCode = resultStr.getBody();
		 return responseCode;
		 
	}
	/**
	 * 手机订单查询
	 * 
	 * */
	public static String mobileOrdersta(String ordercode ,String key){
		
		 String url =  "http://op.juhe.cn/ofpay/mobile/ordersta?orderid="+ordercode+"&key="+key;
		
		 RestTemplate restTemplate=new RestTemplate();
		 ResponseEntity<String> resultStr = restTemplate.exchange(url,HttpMethod.GET, null, String.class);
		 String responseCode = resultStr.getBody();
		 return responseCode;
		 
	}
	
	
}
