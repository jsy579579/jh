package com.jh.paymentgateway.util.yf;

import java.util.Map;

public class RequestJson {

	private String code;
	private Map<String, Object> request;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public Map<String, Object> getRequest() {
		return request;
	}

	public void setRequest(Map<String, Object> request) {
		this.request = request;
	}
}
