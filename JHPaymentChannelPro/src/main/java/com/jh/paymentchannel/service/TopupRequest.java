package com.jh.paymentchannel.service;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;


public interface TopupRequest {

	public  Map<String, String> topupRequest(Map<String,Object> params) throws Exception;
	
}
