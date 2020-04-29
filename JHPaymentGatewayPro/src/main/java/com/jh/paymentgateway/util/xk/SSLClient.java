package com.jh.paymentgateway.util.xk;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;

import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;


public class SSLClient {
	public static CloseableHttpClient creatSSLClient() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException{
		SSLContext context = SSLContextBuilder.create().loadTrustMaterial(null, new TrustStrategy() {
			//信任所有
			@Override
			public boolean isTrusted(X509Certificate[] arg0, String arg1)
					throws CertificateException {
				
				return true;
			}
		}).build();
		SSLConnectionSocketFactory factory = new SSLConnectionSocketFactory(context);
		return HttpClients.custom().setSSLSocketFactory(factory).build();
	}
}
