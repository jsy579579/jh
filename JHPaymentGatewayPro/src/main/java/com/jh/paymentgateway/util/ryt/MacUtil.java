package com.jh.paymentgateway.util.ryt;


import javax.crypto.Cipher;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MacUtil {
	
	
	private static final Logger logger = LoggerFactory.getLogger(MacUtil.class);


	/**
	 * DES ECB 加密
	 * 
	 * @param key
	 * @param plainData
	 * @return
	 */
	public static byte[] desEncrypt(byte[] key, byte[] plainData) {
		if (key.length < 8) {
			logger.debug("密钥长度不足8");
			return null;
		}
		if (plainData.length % 8 != 0) {
			logger.debug("待加密数据长度不是8的倍数");
			return null;
		}
		try {
			DESKeySpec deskey = new DESKeySpec(key);
			SecretKeySpec keySpec = new SecretKeySpec(deskey.getKey(), "DES");
			Cipher cipher = Cipher.getInstance("DES/ECB/NoPadding");
			cipher.init(Cipher.ENCRYPT_MODE, keySpec);
			byte[] data = cipher.doFinal(plainData);
			return data;
		} catch (Exception e) {
			logger.debug("DES加密异常", e);
			return null;
		}
	}


	public static byte[] des3Encrypt(byte[] key, byte[] plainData) {
		if (key.length != 16 && key.length != 24) {
			logger.debug("密钥长度不是16或者24");
			return null;
		}
		byte[] key1 = new byte[24];
		if (key.length == 16) {
			System.arraycopy(key, 0, key1, 0, 16);
			System.arraycopy(key, 0, key1, 16, 8);
		}
		if (key.length == 24) {
			System.arraycopy(key, 0, key1, 0, 24);
		}
		if (plainData.length % 8 != 0) {
			logger.debug("待加密数据长度不是8的倍数");
			return null;
		}
		try {
			SecretKeySpec keySpec = new SecretKeySpec(key1, "DESede");
			Cipher cipher = Cipher.getInstance("DESede/ECB/NoPadding");
			cipher.init(Cipher.ENCRYPT_MODE, keySpec);
			byte[] data = cipher.doFinal(plainData);
			return data;
		} catch (Exception e) {
			logger.debug("3DES加密异常", e);
			return null;
		}
	}

	public static byte[] des3Decrypt(byte[] key, byte[] cryptData) {
		if (key.length != 16 && key.length != 24) {
			logger.debug("密钥长度不是16或者24");
			return null;
		}
		byte[] key1 = new byte[24];
		if (key.length == 16) {
			System.arraycopy(key, 0, key1, 0, 16);
			System.arraycopy(key, 0, key1, 16, 8);
		}
		if (key.length == 24) {
			System.arraycopy(key, 0, key1, 0, 24);
		}
		if (cryptData.length % 8 != 0) {
			logger.debug("待解密数据长度不是8的倍数");
			return null;
		}
		try {
			SecretKeySpec keySpec = new SecretKeySpec(key1, "DESede");
			Cipher cipher = Cipher.getInstance("DESede/ECB/NoPadding");
			cipher.init(Cipher.DECRYPT_MODE, keySpec);
			byte[] data = cipher.doFinal(cryptData);
			return data;
		} catch (Exception e) {
			logger.debug("3DES解密异常", e);
			return null;
		}
	}


	/**
	 * 生成密钥验证串
	 * 
	 * @param wkey
	 * @return
	 */
	public static String keyCheckValue(byte[] wkey) {
		if (wkey.length != 8 && wkey.length != 16) {
			return null;
		}
		byte[] data = { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };
		byte[] encData = null;
		if (wkey.length == 8)
			encData = desEncrypt(wkey, data);
		if (wkey.length == 16)
			encData = des3Encrypt(wkey, data);
		if (encData == null) {
			return null;
		}
		String encDataStr = ByteArrayUtil.byteArray2HexString(encData);
		return encDataStr.substring(0, 8);
	}

	
}
