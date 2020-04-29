package com.jh.paymentgateway.util.ap.security;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by pthahnil on 2019/5/8.
 */
public class TrippleDesUtil {

	private static final String CHARSET = "utf-8";

	private static final String ALGORITHM = "DESede";

	/**
	 * cbc padding加密
	 * @param content
	 * @param key
	 * @return
	 * @throws Exception
	 */
	public static String cbcEncryt(String content, String key) throws Exception {
		byte[] contentBytes = content.getBytes(CHARSET);
		byte[] keyBytes = key.getBytes(CHARSET);

		byte[] encryptedBytes = process(contentBytes, keyBytes, PaddingMode.DES_CBC, Cipher.ENCRYPT_MODE);
		byte[] enc64 = Base64.encodeBase64(encryptedBytes);
		return new String(enc64);
	}

	/**
	 * ecb padding加密
	 * @param content
	 * @param key
	 * @return
	 * @throws Exception
	 */
	public static String ecbEncryt(String content, String key) throws Exception {
		byte[] contentBytes = content.getBytes(CHARSET);
		byte[] keyBytes = key.getBytes(CHARSET);

		byte[] encryptedBytes = process(contentBytes, keyBytes, PaddingMode.DES_ECB, Cipher.ENCRYPT_MODE);
		byte[] enc64 = Base64.encodeBase64(encryptedBytes);
		return new String(enc64);
	}

	/**
	 * cbc padding 解密
	 * @param source
	 * @param decryptionKey
	 * @return
	 * @throws Exception
	 */
	public static String cbcDecrypt(String source, String decryptionKey) throws Exception {
		byte[] base64 = source.getBytes(CHARSET);

		byte[] content = Base64.decodeBase64(base64);
		byte[] key = decryptionKey.getBytes(CHARSET);

		byte[] decryptedInfo = process(content, key, PaddingMode.DES_CBC, Cipher.DECRYPT_MODE);
		return new String(decryptedInfo);
	}

	/**
	 * ecb padding 解密
	 * @param source
	 * @param decryptionKey
	 * @return
	 * @throws Exception
	 */
	public static String ecbDecrypt(String source, String decryptionKey) throws Exception {
		byte[] base64 = source.getBytes(CHARSET);

		byte[] content = Base64.decodeBase64(base64);
		byte[] key = decryptionKey.getBytes(CHARSET);

		byte[] decryptedInfo = process(content, key, PaddingMode.DES_ECB, Cipher.DECRYPT_MODE);
		return new String(decryptedInfo);
	}

	/**
	 * 加/解密
	 * @param content
	 * @param key
	 * @param padding
	 * @param processMode
	 * @return
	 * @throws Exception
	 */
	protected static byte[] process(byte[] content, byte[] key, PaddingMode padding, int processMode)
			throws Exception {
		final SecretKey secretKey = new SecretKeySpec(key, ALGORITHM);
		final Cipher cipher = Cipher.getInstance(padding.getType());
		IvParameterSpec iv = null;
		if(padding == PaddingMode.DES_CBC){
			byte[] icv = new byte[8];
			System.arraycopy(key, 0, icv, 0, 8);
			iv = new IvParameterSpec(icv);
		}
		cipher.init(processMode, secretKey, iv);
		return cipher.doFinal(content);
	}


	public static void main(String[] args) throws Exception {
		String key = "86KPi4OEI64E7rrr57o07wUr";

		String infoToEncrypt = "{\"dealer_id\":\"20479991\"}";

		/*String cbcEncrypted = cbcEncryt(infoToEncrypt, key);
		System.out.println("cbcEncryt: " + cbcEncrypted);

		String cbcDecrypted = cbcDecrypt(cbcEncrypted, key);
		System.out.println("cbcDecrypted: " + cbcDecrypted);

		String ecbEncrypted = ecbEncryt(infoToEncrypt, key);
		System.out.println("ecbEncryt: " + ecbEncrypted);

		String ecbDecrypted = ecbDecrypt(ecbEncrypted, key);
		System.out.println("ecbDecrypted: " + ecbDecrypted);*/

		/*String info = "0xfLqVX3/QLqp0HYwbkhzS6BV1gGkteDG+pM0vmP/K31fkQSAx/oWFBv+l1MNQm8Cl6u1HiV0QI57XAe5IFfMDq430myV4k9TI/Vc8agq8Xsixpi+LqgV9ptGGJBhmRrvCciVGc7ZhiDdcwXAbV/DVCM6zey711AUDeI0Kcez+rWf1E5mZ8MmMAvozpMu2EOWz5PY5hMl3NQlNxaKxx2dPQARkop4VZxQaskeREi6luC1hbxfccHuAJ5Vw9JrL1KP1I8omY0NXUXtZZ9euO5oZwi6CNwReU6f16+hwGL4MIsXY9AzxXtDGeg//wKV6w3fxryhs6CHf/4DAHIwxvac9mH7ZOyZyMJF2ouUuXplia7x+DWy3RsQwKTUdVeKdWOL6TUKKfcSsHiMKzAnft6C6NUYTstinIQO8O8FmTQPjRYI4BYiBMqYRDjQCK2NEVv8ByTQbOyb3NJByexu2e8EeTtVESy1DNGJXdsxbDeeyGB5LVfJ5c9tP0OX7Td1O1QjWz8DFQctlDyD4mpWZxUg9aWAa9v7ulaxg/2Yu31Cwq+JWmxcGhlP/19zAN0s0XlnJj0XBvcpRfQ0X4fE4WxuRV8hQQXYo9PLilzxvZ072GwkmPXrFnGrO0VHnCuZ9/Aw8yDGOADgxCm6JZqKgBHw/sN7ppR219sM3UTxQgEbHyhtcRQrSUxBiihNM/F4CWpTmTnLjFAxU2BWkHekE98FA==";
		String decrypted = cbcDecrypt(info, key);
		System.out.println(decrypted);*/
	}

}
