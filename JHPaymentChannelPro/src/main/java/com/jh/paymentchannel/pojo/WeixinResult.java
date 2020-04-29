package com.jh.paymentchannel.pojo;

import java.io.Serializable;

public class WeixinResult implements Serializable{

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String codeUrl;
	
	private String imgUrl;

	public String getCodeUrl() {
		return codeUrl;
	}

	public void setCodeUrl(String codeUrl) {
		this.codeUrl = codeUrl;
	}

	public String getImgUrl() {
		return imgUrl;
	}

	public void setImgUrl(String imgUrl) {
		this.imgUrl = imgUrl;
	}
	
	
	
	
}
