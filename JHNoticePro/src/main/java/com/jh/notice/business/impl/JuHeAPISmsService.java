package com.jh.notice.business.impl;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class JuHeAPISmsService {
	
	@Async("myAsync")
	public static   String  sendSms(String mobile, String tpl_id,String smsUrl,String key,Map<String, String> params){
		
		 Iterator<Map.Entry<String, String>> it = params.entrySet().iterator();
		 StringBuffer  paramvalues   = new StringBuffer();  
		 while (it.hasNext()) {
			 	Map.Entry<String, String> entry = it.next();
			 	System.out.println("key= " + entry.getKey() + " and value= " + entry.getValue());
			 	paramvalues.append("#"+entry.getKey()+"#="+entry.getValue()+"&");
		 }
		 String param = paramvalues.toString();
		 param = param.substring(0, param.length()-1);
		 String url = "";
		
		 try {
			url = smsUrl + "?mobile="+mobile+"&tpl_id="+tpl_id+"&tpl_value="+URLEncoder.encode(param, "UTF-8")+"&key=" + key;
//			url = "http://v.juhe.cn/sms/send?mobile="+mobile+"&tpl_id="+tpl_id+"&tpl_value="+URLEncoder.encode(param, "UTF-8")+"&key=1213e5a1ca48e3648e3a0221e0d941d4";
		 } catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		 }
		 RestTemplate restTemplate=new RestTemplate();
		 ResponseEntity<String> resultStr = restTemplate.exchange(url,HttpMethod.GET, null, String.class);
		 String responseCode = resultStr.getBody();
		 return responseCode;
	}
	
}
