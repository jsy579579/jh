package com.jh.paymentgateway.util.yf;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import org.apache.commons.codec.binary.Base64;

public class SignUtil {
	private static final char[] bcdLookup = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

	public static final String bytesToHexStr(byte[] bcd) {
		StringBuffer s = new StringBuffer(bcd.length * 2);

		for (int i = 0; i < bcd.length; i++) {
			s.append(bcdLookup[(bcd[i] >>> 4 & 0xF)]);
			s.append(bcdLookup[(bcd[i] & 0xF)]);
		}

		return s.toString();
	}

	public static final byte[] hexStrToBytes(String s) {
		byte[] bytes = new byte[s.length() / 2];

		for (int i = 0; i < bytes.length; i++) {
			bytes[i] = ((byte) Integer.parseInt(s.substring(2 * i, 2 * i + 2), 16));
		}

		return bytes;
	}

	public static byte[] sign(byte[] priKeyText, String plainText) {
		try {
			PKCS8EncodedKeySpec priPKCS8 = new PKCS8EncodedKeySpec(priKeyText);
			KeyFactory keyf = KeyFactory.getInstance("RSA");
			PrivateKey prikey = keyf.generatePrivate(priPKCS8);
			Signature signet = java.security.Signature.getInstance("MD5withRSA");
			signet.initSign(prikey);
			signet.update(plainText.getBytes());
			byte[] signed = Base64.encodeBase64(signet.sign());
			return signed;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}