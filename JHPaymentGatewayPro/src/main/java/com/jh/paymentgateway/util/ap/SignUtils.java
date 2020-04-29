package com.jh.paymentgateway.util.ap;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;

/**
 * Created by pthahnil on 2019/5/28.
 */
public class SignUtils {

	/**
	 *  签名
	 * @param privateKey
	 * @param data
	 * @param algorithm
	 * @return
	 */
	public static byte[] sign(PrivateKey privateKey, byte[] data, String algorithm) {
		try {
			Signature signature = Signature.getInstance(algorithm);
			signature.initSign(privateKey);
			signature.update(data);
			byte[] sign = signature.sign();
			return sign;
		} catch (Exception var5) {
			throw new RuntimeException(var5);
		}
	}

	/**
	 * 验签
	 * @param publicKey
	 * @param data
	 * @param sign
	 * @param algorithm
	 * @return
	 */
	public static boolean valifySign(PublicKey publicKey, byte[] data, byte[] sign, String algorithm) {
		try {
			Signature signature = Signature.getInstance(algorithm);
			signature.initVerify(publicKey);
			signature.update(data);
			return signature.verify(sign);
		} catch (Exception var5) {
			throw new RuntimeException(var5);
		}
	}
}
