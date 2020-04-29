package com.jh.paymentgateway.util.np;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * AESECB模式加密解密
 * 
 * @author ips
 *
 */
public class Aes128Util {

	/**
	 * 默认编码方式
	 */
	private static final String CHARSET = "UTF-8";

	/**
	 * AES加密
	 * 
	 * @param text
	 * @param key
	 * @return
	 * @throws Exception
	 */
	public static String encrypt(String text, String key) {

		try {
			return encrypt(text, key, CHARSET);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	/**
	 * AES解密
	 * 
	 * @param text
	 * @param key
	 * @return
	 * @throws Exception
	 */
	public static String decrypt(String text, String key) {
		try {
			return decrypt(text, key, CHARSET);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	/**
	 * 
	 * @param text
	 * @param key
	 * @return
	 * @throws Exception
	 */
	public static String encrypt(String text, String key, String charset) {
		try {
			if (key == null) {
				throw new RuntimeException("key is null");
			}
			if (16 != key.length() && 24 != key.length() && 32 != key.length()) {
				throw new RuntimeException("key must be 16/24/32");
			}
			byte[] raw = key.getBytes(charset);
			SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");// "算法/模式/补码方式"
			cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
			byte[] encrypted = cipher.doFinal(text.getBytes(charset));
			return parseByte2HexStr(encrypted);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	/**
	 * 
	 * @param text
	 * @param key
	 * @return
	 * @throws Exception
	 */
	public static String decrypt(String text, String key, String charset) throws Exception {
		try {
			// 判断Key是否正确
			if (key == null) {
				throw new RuntimeException("key is null");
			}
			if (16 != key.length() && 24 != key.length() && 32 != key.length()) {
				throw new RuntimeException("key must be 16/24/32");
			}
			byte[] raw = key.getBytes(charset);
			SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
			cipher.init(Cipher.DECRYPT_MODE, skeySpec);
			byte[] encrypted1 = parseHexStr2Byte(text);
			try {
				byte[] original = cipher.doFinal(encrypted1);
				String originalString = new String(original, charset);
				return originalString;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * 二进制转16进制
	 * 
	 * @param buf
	 * @return
	 */
	public static String parseByte2HexStr(byte buf[]) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < buf.length; i++) {
			String hex = Integer.toHexString(buf[i] & 0xFF);
			if (hex.length() == 1) {
				hex = '0' + hex;
			}
			sb.append(hex.toLowerCase());
		}
		return sb.toString();
	}

	/**
	 * 字符串转二进制
	 * 
	 * @param hexStr
	 * @return
	 */
	public static byte[] parseHexStr2Byte(String hexStr) {
		if (hexStr.length() < 1)
			return null;
		byte[] result = new byte[hexStr.length() / 2];
		for (int i = 0; i < hexStr.length() / 2; i++) {
			int high = Integer.parseInt(hexStr.substring(i * 2, i * 2 + 1), 16);
			int low = Integer.parseInt(hexStr.substring(i * 2 + 1, i * 2 + 2), 16);
			result[i] = (byte) (high * 16 + low);
		}
		return result;
	}
}
