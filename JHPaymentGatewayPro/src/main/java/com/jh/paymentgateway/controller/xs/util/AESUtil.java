package com.jh.paymentgateway.controller.xs.util;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;


/**
 * @version V1.0
 * @desc AES 加密工具类
 */
public class AESUtil {
	/*
	 * 加密用的Key 可以用26个字母和数字组成 此处使用AES-128-CBC加密模式，key需要为16位。
	 */
	// private static String sKey = "wSWWafWV9J1Wbs0B";
	// private static String ivParameter = "wSWWafWV9J1Wbs0B";

	/**
	 * 加密
	 * 
	 * @param sSrc
	 * @return
	 * @throws Exception
	 */
	public static String encrypt(String sSrc, String sKey, String ivParameter) {
		Cipher cipher;
		try {
			cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			byte[] raw = sKey.getBytes();
			SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
			IvParameterSpec iv = new IvParameterSpec(ivParameter.getBytes());// 使用CBC模式，需要一个向量iv，可增加加密算法的强度
			cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
			byte[] encrypted = cipher.doFinal(sSrc.getBytes("utf-8"));
			return Base64.encode(encrypted);// 此处使用BASE64做转码。
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 解密
	 * 
	 * @param sSrc
	 * @param ssKey
	 * @return
	 * @throws Exception
	 */
	public static String decrypt(String sSrc, String ssKey, String ivParameter) {
		try {
			byte[] raw = ssKey.getBytes("ASCII");
			SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			IvParameterSpec iv = new IvParameterSpec(ivParameter.getBytes());
			cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
			byte[] encrypted1 = Base64.decode(sSrc);
			byte[] original = cipher.doFinal(encrypted1);
			String originalString = new String(original, "utf-8");
			return originalString;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public static void main(String[] args) throws Exception {
		// 需要加密的字串
		String cSrc = "789";
		System.out.println(cSrc);
		// 加密
		long lStart = System.currentTimeMillis();
		String enString = AESUtil.encrypt(cSrc, "wSWWafWV9J1Wbs0B", "wSWWafWV9J1Wbs0B");
		System.out.println("加密后的字串是：" + enString);

		long lUseTime = System.currentTimeMillis() - lStart;
		System.out.println("加密耗时：" + lUseTime + "毫秒");
		// 解密
		lStart = System.currentTimeMillis();
		String DeString = AESUtil.decrypt("6v25hVDKtXv3Cy+Wn3WER8Wjf5ghvrCIVjtkCqis13Q=", "wSWWafWV9J1Wbs0B", "wSWWafWV9J1Wbs0B");
		System.out.println("解密后的字串是：" + DeString);
		lUseTime = System.currentTimeMillis() - lStart;
		System.out.println("解密耗时：" + lUseTime + "毫秒");
	}

	public static String unicode(String source) {
		StringBuffer sb = new StringBuffer();
		char[] source_char = source.toCharArray();
		String unicode = null;
		for (int i = 0; i < source_char.length; i++) {
			unicode = Integer.toHexString(source_char[i]);
			if (unicode.length() <= 2) {
				unicode = "00" + unicode;
			}
			sb.append("\\u" + unicode);
		}
		System.out.println(sb);
		return sb.toString();
	}

	public static String decodeUnicode(String unicode) {
		StringBuffer sb = new StringBuffer();

		String[] hex = unicode.split("\\\\u");

		for (int i = 1; i < hex.length; i++) {
			int data = Integer.parseInt(hex[i], 16);
			sb.append((char) data);
		}
		return sb.toString();
	}
}