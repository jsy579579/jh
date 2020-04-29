
package com.jh.paymentgateway.util.jf;

import java.security.PrivateKey;
import java.security.PublicKey;

/**
* @author cmt  
* @E-mail:29572320@qq.com
* @version Create on:  2017年4月28日 上午11:32:07
* Class description
*/

public class CommunicationProvideSecurityWarper {
	
	/**
	 * 用platformPriKey解密encryptKey
	 * 
	 * @param platformPrivateKey
	 * @return
	 */
	public static String getPartnerAESKeyByPlatformPrivateKey(String cipherB64PartnerAESkey,  PrivateKey platformPrivateKey){
		
		String partnerAESKey = null;
		try {
			byte [] cipherPartnerAESkeyBytes  = Base64.decode(cipherB64PartnerAESkey);
			partnerAESKey = RSA.privateKeyDecrypt(cipherPartnerAESkeyBytes, platformPrivateKey);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return partnerAESKey;
		
	}
	

	
	/**
	 * 用partnerAESKey解密encryptData
	 * @return
	 */
	public static String getPlainTextByAESKey(String cipherB64PlainText,  String partnerAESKey){
		String plainText=null;
		try {
			byte [] cipherPlainBytes =  Base64.decode(cipherB64PlainText);
			plainText = AES.decode(cipherPlainBytes, partnerAESKey);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return plainText;
		
	}
	
	/**
	 * 用partnerPubKey验证签名
	 * @return
	 */
	public static boolean checkSignB64PlainTextByPartnerPublicKey(String plainText, String signB64PlainText,  PublicKey partnerPublicKey){
		//byte [] cipherPlainBytes =  Base64.decode(cipherB64PlainText);
		boolean flag = false;
		try {
			byte [] signPlainBytes =  Base64.decode(signB64PlainText);
			flag = RSA.verifyPublicKeyDigitalSign(plainText, signPlainBytes, partnerPublicKey);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return flag;
		
	}
	

	

}
