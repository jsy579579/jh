package com.jh.paymentgateway.util.ffzc;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * 卡盟加密工具类
 * 3des加密后，使用base64，utf8编码做加密。
 * @author Administrator
 *
 */
public class KmSecurityUtil {
	/** 编码字符集 **/	
	private static String CHAR_ENCODING = "UTF-8";
	
	/*public static void main(String[] args) throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException, IllegalStateException {
		//加密KEY
		String desKey = "Gr7TFVDIlrdBugdpHU8R7AVO";
		
		String payerName = KmSecurityUtil.encode(desKey,"张三峰");
		String idCardNo = KmSecurityUtil.encode(desKey,"130127199103120925");
		String cardNo = KmSecurityUtil.encode(desKey,"6226220282587115");
		System.out.println("payerName="+payerName);
		System.out.println("idCardNo="+idCardNo);
		System.out.println("cardNo="+cardNo);
	}*/

	/**
	 * Base64编码
	 * @param key
	 * @param data
	 * @return
	 */
	public static String encode(String key, String data) {
		try {
			byte[] keyByte = key.getBytes(CHAR_ENCODING);
			byte[] dataByte = data.getBytes(CHAR_ENCODING);
			byte[] valueByte = KmSecurityUtil.des3Encryption(keyByte, dataByte);
			String value = new String(Base64.encodeBase64(valueByte), CHAR_ENCODING);
			return value;
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * des3Encryption加密
	 * @param key
	 * @param data
	 * @return
	 * @throws NoSuchPaddingException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 * @throws BadPaddingException
	 * @throws IllegalBlockSizeException
	 * @throws IllegalStateException
	 */
	public static byte[] des3Encryption(byte[] key, byte[] data) throws
	      NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException,
	      BadPaddingException, IllegalBlockSizeException, IllegalStateException {
	    final String Algorithm = "DESede"; 

	    SecretKey deskey = new SecretKeySpec(key, Algorithm);

	    Cipher c1 = Cipher.getInstance(Algorithm);
	    c1.init(Cipher.ENCRYPT_MODE, deskey);
	    return c1.doFinal(data);
	  }

}
