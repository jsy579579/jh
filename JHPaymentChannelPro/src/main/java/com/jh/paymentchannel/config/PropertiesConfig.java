package com.jh.paymentchannel.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

@Component
@RefreshScope
public class PropertiesConfig{
	
	@Value("${juhe.auth4.url}")
	private String auth4Url;
	
	@Value("${juhe.auth4.key}")
	private String auth4Key;
	
	@Value("${juhe.cardlocation.url}")
	private String cardLocationUrl;
	
	@Value("${juhe.cardlocation.key}")
	private String cardLocationKey;
	
	@Value("${juhe.realname.url}")
	private String realNameUrl;
	
	@Value("${juhe.realname.key}")
	private String realNameKey;
	
	public String getAuth4Url() {
		return auth4Url;
	}

	public String getAuth4Key() {
		return auth4Key;
	}

	public String getCardLocationUrl() {
		return cardLocationUrl;
	}

	public String getCardLocationKey() {
		return cardLocationKey;
	}

	public String getRealNameUrl() {
		return realNameUrl;
	}

	public String getRealNameKey() {
		return realNameKey;
	}
	
}
