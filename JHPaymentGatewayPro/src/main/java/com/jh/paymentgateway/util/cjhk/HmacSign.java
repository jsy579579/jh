package com.jh.paymentgateway.util.cjhk;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * hmac签名算法工具类
 * 
 * @author blakeyuan
 *
 */
public class HmacSign {

	/**
	 * 对报文进行采用SHA进行hmac签名
	 * 
	 * @param value
	 *            - 字符串
	 * @param key
	 *            - 密钥
	 * @return - 签名结果，hex字符串
	 */
	public static String signToBase64(String value, String key) {
		try {
			return new String(Base64.encode((sign(
					value.getBytes(ConfigureEncryptAndDecrypt.CHAR_ENCODING),
					key.getBytes(ConfigureEncryptAndDecrypt.CHAR_ENCODING)))));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("hmacsign fail!", e);
		}
	}

	public static String signToHex(String value, String key) {
		try {
			return Hex.toHex(sign(
					value.getBytes(ConfigureEncryptAndDecrypt.CHAR_ENCODING),
					key.getBytes(ConfigureEncryptAndDecrypt.CHAR_ENCODING)));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("hmacsign fail!", e);
		}
	}

	public static byte[] sign(byte[] data, byte[] key) {
		return sign(data, key, ConfigureEncryptAndDecrypt.MD5_ALGORITHM);
	}

	public static byte[] sign(byte[] data, byte[] key, String alg) {
		byte k_ipad[] = new byte[64];
		byte k_opad[] = new byte[64];
		Arrays.fill(k_ipad, key.length, 64, (byte) 54);
		Arrays.fill(k_opad, key.length, 64, (byte) 92);
		for (int i = 0; i < key.length; i++) {
			k_ipad[i] = (byte) (key[i] ^ 0x36);
			k_opad[i] = (byte) (key[i] ^ 0x5c);
		}
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance(alg);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("hmacsign fail!", e);
		}
		md.update(k_ipad);
		md.update(data);
		byte dg[] = md.digest();
		md.reset();
		md.update(k_opad);
		md.update(dg, 0, 16);
		dg = md.digest();
		return dg;
	}
}
