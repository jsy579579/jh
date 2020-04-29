package com.jh.paymentchannel.util.abroad;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

public class RSAUtils {
	public static String rsaSign(String content, String privateKey, String charset, String algorithm) throws SignatureException {
		try {
			PrivateKey priKey = getPrivateKeyFromPKCS8("RSA", new ByteArrayInputStream(privateKey.getBytes()));
 
			Signature signature = Signature.getInstance(algorithm);//MD5withRSA  SHA1WithRSA SHA256WithRSA
			signature.initSign(priKey);
			if (StringUtils.isEmpty(charset)) {
				signature.update(content.getBytes());
			} else {
				signature.update(content.getBytes(charset));
			}
			byte[] signed = signature.sign();
			return new String(Base64.encodeBase64(signed));
		} catch (Exception e) {
			throw new SignatureException("RSAcontent = " + content + "; charset = " + charset, e);
		}
	}
 
	public static boolean doCheck(String content, String sign, String publicKey, String charset, String algorithm) throws SignatureException {
		try {
			PublicKey pubKey = getPublicKeyFromX509("RSA", new ByteArrayInputStream(publicKey.getBytes()));
 
			Signature signature = Signature.getInstance(algorithm);//MD5withRSA  SHA1WithRSA SHA256WithRSA
			signature.initVerify(pubKey);
			byte[] encodedKey = content.getBytes(charset);
			signature.update(encodedKey);
			// signature.update(getContentBytes(content, charset));
			return signature.verify(Base64.decodeBase64(sign.getBytes()));
		} catch (Exception e) {
			throw new SignatureException(
					"RSA验证签名[content = " + content + "; charset = " + charset + "; signature = " + sign + "]发生异常!", e);
		}
	}
 
	private static PublicKey getPublicKeyFromX509(String algorithm, InputStream ins) throws NoSuchAlgorithmException {
		try {
			KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
 
			StringWriter writer = new StringWriter();
			// StreamUtil.io(new InputStreamReader(ins), writer);
			// byte[] encodedKey = writer.toString().getBytes();
			byte[] encodedKey = IOUtils.toByteArray(ins);
			// 先base64解码
			encodedKey = Base64.decodeBase64(encodedKey);
			return keyFactory.generatePublic(new X509EncodedKeySpec(encodedKey));
		} catch (IOException ex) {
			// 不可能发生
		} catch (InvalidKeySpecException ex) {
			// 不可能发生
		}
		return null;
	}
 
	public static PrivateKey getPrivateKeyFromPKCS8(String algorithm, InputStream ins) throws Exception {
		if (ins == null || StringUtils.isEmpty(algorithm)) {
			return null;
		}
 
		KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
		byte[] encodedKey = IOUtils.toByteArray(ins);
 
		encodedKey = Base64.decodeBase64(encodedKey);
		return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(encodedKey));
	}
}
