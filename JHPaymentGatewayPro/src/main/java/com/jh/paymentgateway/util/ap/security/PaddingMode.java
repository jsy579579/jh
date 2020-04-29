package com.jh.paymentgateway.util.ap.security;

/**
 * Created by pthahnil on 2019/5/28.
 */
public enum PaddingMode {
	DES_CBC("DESede/CBC/PKCS5Padding"),
	DES_ECB("DESede/ECB/PKCS5Padding"),
	AES_CBC("AES/CBC/PKCS5Padding"),
	AES_ECB("AES/ECB/PKCS5Padding"),
	;
	String type;

	public String getType() {
		return type;
	}

	PaddingMode(String type) {
		this.type = type;
	}
}
