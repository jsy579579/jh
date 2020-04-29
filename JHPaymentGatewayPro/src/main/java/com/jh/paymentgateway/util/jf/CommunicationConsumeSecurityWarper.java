
package com.jh.paymentgateway.util.jf;


import java.security.PrivateKey;
import java.security.PublicKey;

/**
* @author cmt  
* @E-mail:29572320@qq.com
* @version Create on:  2017年4月28日 上午11:32:07
* Class description
*/

public class CommunicationConsumeSecurityWarper {
	
	
	
	/**
	 * 生成128bit 16位AES密钥
	 * @return
	 */
	public static String getRandomAESKey(){
		return AES.generateRandomKey();
	}
	
	
	
	/**
	 * 用partnerAESKey加密请求报文
	 * @param plainText
	 * @param AESkey
	 * @return
	 */
	public static String  getCipherB64PlainTextByPartnerAESKey(String plainText,String AESkey){
		byte [] plainBytes =  AES.encode(plainText, AESkey);
		return Base64.encode(plainBytes);
		
	}
	
	
	
	/**
	 * 用partnerPriKey签名请求报文 signtData
	 * @param plainText
	 * @param partnerPrivateKey
	 * @return RSA.digitalPrivateKeySign(text, privatekeypartner);
	 */
	public static String getSignB64PlainTextByPartnerPrivateKey(String plainText ,  PrivateKey partnerPrivateKey ){
		byte[]  plainBytes =	RSA.digitalPrivateKeySign(plainText, partnerPrivateKey);
		return Base64.encode(plainBytes);
		
	}
	
	
	
	/**
	 * 用platformPubKey加密partnerAESKey
	 * @return
	 */
	public static String getCipherB64AESKeyByplatformPublicKey(String partnerAESKey, PublicKey platformPublicKey){
		 byte[] cipherPartnerAESKey =	RSA.publicKeyEncrypt(partnerAESKey, platformPublicKey);
		return Base64.encode(cipherPartnerAESKey);
		
	}

}
