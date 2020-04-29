package com.jh.paymentgateway.util.yf;

import java.security.PrivateKey;
import java.security.PublicKey;



public class LocalUtil {

	public static PublicKey loadPublicKey(String publicKeyData) {

		try {
			return XYRSAUtil.loadPublicKeyBase64(publicKeyData);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	
	public static PrivateKey loadPrivateKey(String privateKeyData) {

		try {
			return XYRSAUtil.loadPrivateKeyBase64(privateKeyData);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static PrivateKey loadPrivatePath(String privatePath) {

		try {
			return XYRSAUtil.loadKeyBase64(privatePath);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	
	public static boolean verifySignature(byte[] publickey, String hexSigned, byte[] toSign) {
		try {
			PublicKey key = XYRSAUtil.loadPublicKey(publickey);
			if (XYRSAUtil.verifySignatureLocal(key, hexSigned, toSign))
				return true;
			else
				return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

	}

	
	public static byte[] sign(byte[] privateKeyData, String plainText) {
		return SignUtil.sign(privateKeyData, plainText);

	}
}
