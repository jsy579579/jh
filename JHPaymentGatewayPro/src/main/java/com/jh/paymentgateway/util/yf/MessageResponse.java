package com.jh.paymentgateway.util.yf;

import java.util.HashMap;
import java.util.Map;
/**
 * 打包组织响应信息
 * @author liy
 *
 */
public class MessageResponse {
	
	//业务响应码
	private String serviceCode;
		
	//响应信息
	private Map<String, Object> response = new HashMap<String, Object>();
	
	
	public String getServiceCode() {
		return serviceCode;
	}

	public void setServiceCode(String serviceCode) {
		this.serviceCode = serviceCode;
	}

	public Map<String, Object> getResponse() {
		return response;
	}

	public void setResponse(Map<String, Object> response) {
		this.response = response;
	}
	
//	/**
//	 * 消息打包
//	 * @param responseCode
//	 * @param creditCardDto
//	 * @return
//	 */
//	public static String packJson(String responseCode,String msg){
//		String ReturnMessage = "{\"code\":\""+responseCode+"\"," +"\"msg\":\""+ msg +"\"}";
//		return ReturnMessage;
//	}
}