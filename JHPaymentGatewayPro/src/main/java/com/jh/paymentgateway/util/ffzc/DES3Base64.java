package com.jh.paymentgateway.util.ffzc;

/*字符串 DESede(3DES) 加密*/

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * php对java的base64注意要用commons-codec-1.7.jar 中的
 * org.apache.commons.codec.binary.Base64; 不用sun.misc.BASE64Encoder().encode()的。
 * 
 * */
public class DES3Base64 {

	private static final String Algorithm = "DESede"; // 定义 加密算法,可用

	// keybyte为加密密钥，长度为24字节
	// src为被加密的数据缓冲区（源）
	public static byte[] encryptMode(byte[] keybyte, byte[] src) {
		try {
			// 生成密钥
			SecretKey deskey = new SecretKeySpec(keybyte, Algorithm);
			// 加密
			Cipher c1 = Cipher.getInstance(Algorithm);
			c1.init(Cipher.ENCRYPT_MODE, deskey);
			return  Base64.encodeBase64(c1.doFinal(src));

		} catch (java.security.NoSuchAlgorithmException e1) {
			e1.printStackTrace();
		} catch (javax.crypto.NoSuchPaddingException e2) {
			e2.printStackTrace();
		} catch (java.lang.Exception e3) {
			e3.printStackTrace();
		}

		return null;
	}

	// keybyte为加密密钥，长度为24字节
	// src为加密后的缓冲区
	public static byte[] decryptMode(byte[] keybyte, byte[] src) {

		src = Base64.decodeBase64(src);

		try {
			// 生成密钥
			SecretKey deskey = new SecretKeySpec(keybyte, Algorithm);
			// 解密
			Cipher c1 = Cipher.getInstance(Algorithm);
			c1.init(Cipher.DECRYPT_MODE, deskey);
			return c1.doFinal(src);
		} catch (java.security.NoSuchAlgorithmException e1) {
			e1.printStackTrace();
		} catch (javax.crypto.NoSuchPaddingException e2) {
			e2.printStackTrace();
		} catch (java.lang.Exception e3) {
			e3.printStackTrace();
		}
		return null;
	}

	// 转换成十六进制字符串
	public static String byte2hex(byte[] b) {
		String hs = "";
		String stmp = "";
		for (int n = 0; n < b.length; n++) {
			stmp = (java.lang.Integer.toHexString(b[n] & 0XFF));
			if (stmp.length() == 1) {
				hs = hs + "0" + stmp;
			} else {
				hs = hs + stmp;
			}
			if (n < b.length - 1) {
				hs = hs + "";
			}
		}
		return hs.toLowerCase();
	}

	/*//
	public static void main(String args[]) {
		// 加密解密测试
		String card_num = "601240381279200";
		String card_pwd = "46926229";
		String str_card = "1|" + card_num + "," + card_pwd;
		String des_key = "b6867240830fau76l0j07dc0";
		byte[] des_card = DES3Base64.encryptMode(des_key.getBytes(),
				str_card.getBytes());
		byte[] result2 = DES3Base64.decryptMode(des_key.getBytes(),
				des_card);
		System.out.println("加密结果 des_card：" + new String(des_card));
		System.out.println("reserve1:"+byte2hex(des_card));
		
		System.out.println("解密结果：" + new String(result2));
	}*/

	public static String encrytTx(String key ,String str){
		byte[] des_card = DES3Base64.encryptMode(key.getBytes(),
				str.getBytes());
		return byte2hex(des_card);
	}
 /**
  * 对应php
  * /yAn3OnhtKjoSlBvJSq7sI4OIi+MHVOTn0iP90rRm7Q=
	2f79416e334f6e68744b6a6f536c42764a5371377349344f49692b4d48564f546e306950393072526d37513d
  * */
}