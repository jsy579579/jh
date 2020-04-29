package com.cardmanager.pro.channel;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cardmanager.pro.business.CreditCardManagerConfigBusiness;
import com.cardmanager.pro.pojo.CreditCardManagerConfig;
import com.cardmanager.pro.util.SpringContextUtil;

@Component
public class ChannelFactory {
	
	@Autowired
	private CreditCardManagerConfigBusiness creditCardManagerConfigBusiness;
	
	private static Map<String,ChannelBaseAPI> CHANNEL_MAP = new HashMap<>();
	
	public ChannelBaseAPI getChannelBaseAPI(String version) {
		ChannelBaseAPI channelBaseAPI = CHANNEL_MAP.get(version);
		if (channelBaseAPI == null) {
			CreditCardManagerConfig cardManagerConfig = creditCardManagerConfigBusiness.findByVersion(version);
			if (cardManagerConfig != null) {
				channelBaseAPI = (ChannelBaseAPI) SpringContextUtil.getBean(cardManagerConfig.getBeanName());
				if (channelBaseAPI != null) {
					CHANNEL_MAP.put(version, channelBaseAPI);
				}
			}
		}
		return channelBaseAPI;
	}
	
	public ChannelRoot getChannelRoot(String version) {
		ChannelRoot channelBaseAPI = (ChannelRoot) CHANNEL_MAP.get(version);
		if (channelBaseAPI == null) {
			CreditCardManagerConfig cardManagerConfig = creditCardManagerConfigBusiness.findByVersion(version);
			if (cardManagerConfig != null) {
				channelBaseAPI = (ChannelRoot) SpringContextUtil.getBean(cardManagerConfig.getBeanName());
			}
		}
		return channelBaseAPI;
	}
}
