package com.jh.paymentgateway.util.yf;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/** 
 * 接收解析上送数据
 * @author liy
 */
public class MessageRequest implements Serializable{

	private static final long serialVersionUID = -4752265663021291191L;
	//业务请求码
	private String serviceCode;
	//请求的request信息
	private Map<String,Object> request = new HashMap<String, Object>();
	
	 
	 
	public String getServiceCode() {
		return serviceCode;
	}
	public void setServiceCode(String serviceCode) {
		this.serviceCode = serviceCode;
	}
	 
	public Map<String, Object> getRequest() {
		return request;
	}
	public void setRequest(Map<String, Object> request) {
		this.request = request;
	}
	
}
