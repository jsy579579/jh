package com.jh.notice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

@Component
@RefreshScope
public class PropertiesConfig{
	
	@Value("${juhe.sms.url}")
	private String smsUrl;
	
	@Value("${juhe.sms.key}")
	private String smsKey;
	

	public String getSmsUrl() {
		return smsUrl;
	}

	public String getSmsKey() {
		return smsKey;
	}

	
	
}
