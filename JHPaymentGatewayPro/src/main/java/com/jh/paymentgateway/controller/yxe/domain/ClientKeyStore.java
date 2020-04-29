package com.jh.paymentgateway.controller.yxe.domain;

import javax.net.ssl.KeyManagerFactory;

/**
 * <b>功能说明:</b>
 */
public class ClientKeyStore {
	private KeyManagerFactory keyManagerFactory;
	
	public ClientKeyStore(KeyManagerFactory keyManagerFactory){
		this.keyManagerFactory = keyManagerFactory;
	}
	
	public KeyManagerFactory getKeyManagerFactory(){
		return keyManagerFactory;
	}
}
