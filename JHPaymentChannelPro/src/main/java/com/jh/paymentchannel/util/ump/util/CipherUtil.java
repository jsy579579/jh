package com.jh.paymentchannel.util.ump.util;

import java.security.Key;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

import com.jh.paymentchannel.util.ump.common.Base64;
import com.jh.paymentchannel.util.ump.common.SunBase64;


/**
 * ***********************************************************************
 * <br>description : 加解密工具类
 * @author      umpay
 * @date        2014-7-25 上午10:16:35
 * @version     1.0  
 ************************************************************************
 */
public class CipherUtil {

	/**
	 * 
	 * <br>description : 用联动公钥加密
	 * @param s
	 * @return
	 * @throws Exception
	 * @version     1.0
	 * @date        2014-7-25上午10:18:17
	 */
	public static String Encrypt(String s)throws Exception{
		X509Certificate cert = PkCertFactory.getCert();
	    byte[] keyBytes = cert.getPublicKey().getEncoded();
	    // 取得公钥
		X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		Key publicKey = keyFactory.generatePublic(x509KeySpec);

		Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
		cipher.init(Cipher.ENCRYPT_MODE, publicKey);
		String str = SunBase64.encode(cipher.doFinal(s.getBytes("GBK"))).replace("\n", "");
		return str;
	}
	
	/**
	 * 
	 * <br>description : 用私钥解密，不指定编码格式
	 * @param merId
	 * @param data
	 * @return
	 * @throws Exception
	 * @version     1.0
	 * @date        2014-8-1下午12:48:37
	 */
	public static String decrypt(String merId,String data)throws Exception{
		PrivateKey pk = PkCertFactory.getPk(merId);
		byte[] b = Base64.decode(data.getBytes());
		byte[] retB = decryptByPrivateKey(pk,b);
		String retData = new String(retB);
		return retData;
	}
	
	/**
	 * 
	 * <br>description : 用私钥解密，指定编码格式
	 * @param merId
	 * @param data
	 * @return
	 * @throws Exception
	 * @version     1.0
	 * @date        2014-8-1下午12:48:37
	 */
	public static String decrypt(String merId,String data,String charset)throws Exception{
		PrivateKey pk = PkCertFactory.getPk(merId);
		byte[] b = Base64.decode(data.getBytes());
		byte[] retB = decryptByPrivateKey(pk,b);
		String retData = new String(retB,charset);
		return retData;
	}
	
	private static byte[] decryptByPrivateKey(PrivateKey pk, byte[] data)
			throws Exception {
		// 对密钥解密
		byte[] keyBytes = pk.getEncoded();
		// 取得私钥
		PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		Key privateKey = keyFactory.generatePrivate(pkcs8KeySpec);
		// 对数据解密
		Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
		cipher.init(Cipher.DECRYPT_MODE, privateKey);
		return cipher.doFinal(data);
	}
}
