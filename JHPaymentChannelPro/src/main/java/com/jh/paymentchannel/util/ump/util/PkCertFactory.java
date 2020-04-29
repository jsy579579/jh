
package com.jh.paymentchannel.util.ump.util;

import java.io.ByteArrayInputStream;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;

/**
 * ***********************************************************************
 * <br>description : 证书工厂类
 * @author      umpay
 * @date        2014-7-25 上午11:43:36
 * @version     1.0  
 ************************************************************************
 */
public class PkCertFactory {

	/**生产平台证书*/
	private static X509Certificate umpCert;
	private final static Map pkMap = new HashMap();
	
	/*private final static String pkSuffix = "e:/key/50211_.key.p8";
	
	private final static String platCertPath="e:/key/cert_2d59.crt";*/
	
	private final static String pkSuffix = "/product/deploy/50211_.key.p8";
	
	private final static String platCertPath="/product/deploy/cert_2d59.crt";
	
	//初始化联动证书
	static{
		try{
			byte[] b = ProFileUtil.getFileByte(platCertPath);
			umpCert = getCert(b);
		}catch(Exception ex){
			RuntimeException rex = new RuntimeException(ex.getMessage());
			rex.setStackTrace(ex.getStackTrace());
			throw rex;
		}
	}
	
	/**
	 * 获取证书字节数组
	 * @return
	 */
	public static byte[] getCertByte(){
		byte[] b = null;
		try{
			b = ProFileUtil.getFileByte(platCertPath);
		}catch(Exception ex){
			RuntimeException rex = new RuntimeException(ex.getMessage());
			rex.setStackTrace(ex.getStackTrace());
			throw rex;
		}
		return b;
	}
	
	/**
	 * 
	 * <br>description : 根据商户号获取私钥
	 * @param merId
	 * @return
	 * @version     1.0
	 * @date        2014-7-25下午01:34:23
	 */
	public static PrivateKey getPk(String merId){
		if(pkMap.containsKey(merId))
			return (PrivateKey)pkMap.get(merId);//返回缓存的私钥
		else{
			synchronized (pkMap) {
				if(pkMap.containsKey(merId))return (PrivateKey)pkMap.get(merId);
				try{
					byte[] b = ProFileUtil.getFileByte(pkSuffix);
					PrivateKey retVal = getPk(b);//获取商户私钥
					pkMap.put(merId, retVal);//缓存商户私钥
					return retVal;
				}catch(Exception ex){
					RuntimeException rex = new RuntimeException(ex.getMessage());
					rex.setStackTrace(ex.getStackTrace());
					throw rex;
				}
			}
		}
	}
	
	public static X509Certificate getCert(){
		return umpCert;
	}
	
	/**
	 * 
	 * <br>description : 生成平台公钥证书对象
	 * @param b
	 * @return
	 * @version     1.0
	 * @date        2014-7-25上午11:56:05
	 */
	private static X509Certificate getCert(byte[] b){
		try {
			ByteArrayInputStream bais = new ByteArrayInputStream(b);
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			X509Certificate x509Certificate = (X509Certificate)cf.generateCertificate(bais);
			return x509Certificate;
		} catch (CertificateException e) {
			RuntimeException rex = new RuntimeException();
			rex.setStackTrace(e.getStackTrace());
			throw rex;
		}
	}
	
	/**
	 * 
	 * <br>description : 生成商户私钥证书对象
	 * @param b
	 * @return
	 * @version     1.0
	 * @date        2014-7-25上午11:56:30
	 */
	private static PrivateKey getPk(byte[] b){
		PKCS8EncodedKeySpec peks = new PKCS8EncodedKeySpec(b);
        KeyFactory kf;
        PrivateKey pk;
		try {
			kf = KeyFactory.getInstance("RSA");
			pk = kf.generatePrivate(peks);
			return pk;
		} catch (NoSuchAlgorithmException e) {
			RuntimeException rex = new RuntimeException();
			rex.setStackTrace(e.getStackTrace());
			throw rex;
		}catch (InvalidKeySpecException e) {
			RuntimeException rex = new RuntimeException();
			rex.setStackTrace(e.getStackTrace());
			throw rex;
		}
	}
	
}
