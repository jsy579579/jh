package com.cardmanager.pro.channel;

import org.springframework.util.LinkedMultiValueMap;

import net.sf.json.JSONObject;

public interface ChannelBaseAPI {
	
	public abstract JSONObject getChannelUserAccount(LinkedMultiValueMap<String, String> requestEntity);
	
	public abstract JSONObject getOrderStatus(LinkedMultiValueMap<String, String> requestEntity,String orderType);
	
	public abstract JSONObject isRegisterToChannel(LinkedMultiValueMap<String, String> requestEntity);
	
}
