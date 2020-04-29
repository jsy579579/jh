package com.jh.paymentgateway.util.ffzc;

import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.IvParameterSpec;


public class Des3Encryption {
	public static final String CHAR_ENCODING = "UTF-8";
	public static final String CHAR_ENCODING2 = "GBK";

	public static byte[] encode(byte[] key, byte[] data) throws Exception {
		return MessageAuthenticationCode.des3Encryption(key, data);
	}

	public static byte[] decode(byte[] key, byte[] value) throws Exception {
		return MessageAuthenticationCode.des3Decryption(key, value);
	}

	public static String encode(String key, String data) {
		try {
			byte[] keyByte = key.getBytes(CHAR_ENCODING);
			byte[] dataByte = data.getBytes(CHAR_ENCODING);
			byte[] valueByte = MessageAuthenticationCode.des3Encryption(keyByte, dataByte);
			String value = new String(Base64.encode(valueByte), CHAR_ENCODING);
			return value;
		} catch (Exception e) {
			return null;
		}
	}

	public static String decode(String key, String value) {
		try {
			byte[] keyByte = key.getBytes(CHAR_ENCODING);
			byte[] valueByte = Base64.decode(value.getBytes(CHAR_ENCODING));
			byte[] dataByte = MessageAuthenticationCode.des3Decryption(keyByte, valueByte);
			String data = new String(dataByte, CHAR_ENCODING);
			return data;
		} catch (Exception e) {
			return null;
		}
	}

	public static String encryptToHex(String key, String data) {
		try {
			byte[] keyByte = key.getBytes(CHAR_ENCODING);
			byte[] dataByte = data.getBytes(CHAR_ENCODING);
			byte[] valueByte = MessageAuthenticationCode.des3Encryption(keyByte, dataByte);
			String value = ConvertUtils.toHex(valueByte);
			return value;
		} catch (Exception e) {
			return null;
		}
	}

	public static String encryptToHexByGBK(String key, String data) {
		try {
			byte[] keyByte = key.getBytes(CHAR_ENCODING2);
			byte[] dataByte = data.getBytes(CHAR_ENCODING2);
			byte[] valueByte = MessageAuthenticationCode.des3Encryption(keyByte, dataByte);
			String value = ConvertUtils.toHex(valueByte);
			return value;
		} catch (Exception e) {
			return null;
		}
	}

	public static String decryptFromHex(String key, String value) {
		try {
			byte[] keyByte = key.getBytes(CHAR_ENCODING);
			byte[] valueByte = ConvertUtils.fromHex(value);
			byte[] dataByte = MessageAuthenticationCode.des3Decryption(keyByte, valueByte);
			String data = new String(dataByte, CHAR_ENCODING);
			return data;
		} catch (Exception e) {
			return null;
		}
	}

	public static String udpEncrypt(String key, String data) {
		try {
			Key k = updGenerateKey(key);
			IvParameterSpec IVSpec = new IvParameterSpec(new byte[8]);
			// 加密
			Cipher c = Cipher.getInstance("DESede/CBC/PKCS5Padding");
			c.init(1, (java.security.Key) k, ((java.security.spec.AlgorithmParameterSpec) (IVSpec)));
			byte output[] = c.doFinal(data.getBytes("UTF-8"));
			return new String(Base64.encode(output), "UTF-8");
		} catch (Exception e) {
			return null;
		}
	}

	public static Key updGenerateKey(String key) {
		try {
			DESedeKeySpec KeySpec = new DESedeKeySpec(UdpHexDecode(key));
			SecretKeyFactory KeyFactory = SecretKeyFactory.getInstance("DESede");
			// 生成密钥
			Key k = ((Key) (KeyFactory.generateSecret(((java.security.spec.KeySpec) (KeySpec)))));
			return k;
		} catch (Exception e) {
			return null;
		}
	}

	public static String udpDecrypt(String key, String data) {
		try {
			byte[] input = Base64.decode(data.getBytes("UTF-8"));
			Key k = updGenerateKey(key);
			IvParameterSpec IVSpec = new IvParameterSpec(new byte[8]);
			Cipher c = Cipher.getInstance("DESede/CBC/PKCS5Padding");
			c.init(2, (java.security.Key) k, ((java.security.spec.AlgorithmParameterSpec) (IVSpec)));
			byte output[] = c.doFinal(input);
			return new String(output, "UTF-8");
		} catch (Exception e) {
			return null;
		}
	}

	public static byte[] UdpHexDecode(String s) {
		byte abyte0[] = new byte[s.length() / 2];
		String s1 = s.toLowerCase();
		for (int i = 0; i < s1.length(); i += 2) {
			char c = s1.charAt(i);
			char c1 = s1.charAt(i + 1);
			int j = i / 2;
			if (c < 'a')
				abyte0[j] = (byte) (c - 48 << 4);
			else
				abyte0[j] = (byte) ((c - 97) + 10 << 4);
			if (c1 < 'a')
				abyte0[j] += (byte) (c1 - 48);
			else
				abyte0[j] += (byte) ((c1 - 97) + 10);
		}
		return abyte0;
	}

	public void afterPropertiesSet() throws Exception {
		// TODO Auto-generated method stub

	}

}
