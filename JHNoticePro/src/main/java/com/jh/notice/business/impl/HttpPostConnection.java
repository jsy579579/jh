package com.jh.notice.business.impl;

import cn.jh.common.tools.http.HttpClient;


public class HttpPostConnection {

	
	public static  String post(String url,  String params){
		
		/**将param 的&分开的拼装成数组*/
		/*String[] paramList = params.split("&");
		MultiValueMap<String, String> requestEntity  = new LinkedMultiValueMap<String, String>();
		for(int i=0; i<paramList.length; i++){
			String temp = paramList[i];
			String[] keyvalues = temp.split("=");
			requestEntity.add(keyvalues[0], keyvalues[1]);
		}
		RestTemplate restTemplate=new RestTemplate();
		String result = restTemplate.postForObject(url, requestEntity, String.class);
		return result;*/
		HttpClient httpClient = new HttpClient();
		String result = httpClient.send(url, params.toString(), "UTF-8", "UTF-8");
    	
    	return result;
		
	}
	
}
