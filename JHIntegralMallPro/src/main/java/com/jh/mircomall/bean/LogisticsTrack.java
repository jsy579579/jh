package com.jh.mircomall.bean;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
/**
 * 物流轨迹属性
 * @author sy
 *
 */
@Component
@ConfigurationProperties(prefix = "logistics")
public class LogisticsTrack {
	// 电商ID
	private String id;
	// 电商加密私钥
	private String pwd;
	// 请求url
	private String url;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getPwd() {
		return pwd;
	}
	public void setPwd(String pwd) {
		this.pwd = pwd;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	
}
