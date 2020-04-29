package com.jh.paymentchannel.util.xj;

import java.security.Key;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;

import org.apache.commons.codec.binary.Base64;

public class CustomInfoTest {
	
	/*public static void main(String[] args) {

		String s = "6225768726920588|李杨|320121199103180036|18512520514";
		
		String re = encode(s, "4f10eb9121da41e8bc056cf0c3308c0a");
		System.out.print(re);
	}*/
	
	public static String encode(String data, String key) {
		try {
			DESedeKeySpec dks = new DESedeKeySpec(key.getBytes());
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DESede");
			Key secretKey = keyFactory.generateSecret(dks);
			Cipher cipher = Cipher.getInstance("DESede/ECB/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, secretKey, new SecureRandom());
			return Base64.encodeBase64String(cipher.doFinal(data.getBytes("utf-8")));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
