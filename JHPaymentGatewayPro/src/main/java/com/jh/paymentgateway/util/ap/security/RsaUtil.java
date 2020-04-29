package com.jh.paymentgateway.util.ap.security;

import com.xiaoleilu.hutool.io.resource.ClassPathResource;
import org.apache.commons.codec.binary.Base64;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.crypto.Cipher;
import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Enumeration;

/**
 * Created by pthahnil on 2019/5/13.
 */
public class RsaUtil {

	/**
	 * 加密算法RSA
	 */
	public static final String ALGORITHM = "RSA";

	/**
	 * 编码
	 */
	private static final String CHAR_SET = "UTF-8";

	/**
	 * 加密
	 * @param content
	 * @param publicKey
	 * @return
	 * @throws Exception
	 */
	public static String encrypt(String content, String publicKey, int keyLength) throws Exception {
		byte[] data = content.getBytes(CHAR_SET);

		BASE64Decoder base64Decoder = new BASE64Decoder();
		byte[] keyBytes = base64Decoder.decodeBuffer(publicKey);

		byte[] encryptedData = process(data, keyBytes, Cipher.ENCRYPT_MODE, keyLength);

		BASE64Encoder encoder = new BASE64Encoder();
		return encoder.encode(encryptedData);
	}

	/**
	 * 解密
	 * @param content
	 * @param privateKey
	 * @return
	 * @throws Exception
	 */
	public static String decrypt(String content, String privateKey, int keyLength) throws Exception {
		byte[] data = Base64.decodeBase64(content);

		BASE64Decoder base64Decoder = new BASE64Decoder();
		byte[] keyBytes = base64Decoder.decodeBuffer(privateKey);

		byte[] decryptedData = process(data, keyBytes, Cipher.DECRYPT_MODE, keyLength);
		return new String(decryptedData, CHAR_SET);
	}

	/**
	 * RSA算法分段加解密数据
	 * @param data
	 * @param keyBytes
	 * @param opmode
	 * @return
	 * @throws Exception
	 */
	private static byte[] process(byte[] data, byte[] keyBytes, int opmode, int keyLength) throws Exception {
		KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);

		Key key;
		if(opmode == Cipher.ENCRYPT_MODE){
			key = keyFactory.generatePublic(new X509EncodedKeySpec(keyBytes));
		} else if(opmode == Cipher.DECRYPT_MODE){
			key = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(keyBytes));
		} else {
			throw new Exception("不支持加解密之外的操作");
		}

		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		cipher.init(opmode, key);

		int maxBlock;
		if (opmode == Cipher.DECRYPT_MODE) {
			maxBlock = keyLength / 8;
		} else {
			maxBlock = keyLength / 8 - 11;
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int offSet = 0;
		byte[] buff;
		int i = 0;

		byte[] resultDatas;
		try {
			while (data.length > offSet) {
				if (data.length - offSet > maxBlock) {
					buff = cipher.doFinal(data, offSet, maxBlock);
				} else {
					buff = cipher.doFinal(data, offSet, data.length - offSet);
				}
				out.write(buff, 0, buff.length);
				i++;
				offSet = i * maxBlock;
			}
			resultDatas = out.toByteArray();
		} catch (Exception e) {
			throw new RuntimeException("加解密阀值为[" + maxBlock + "]的数据时发生异常", e);
		} finally {
			if(null != out){
				try { out.close(); } catch (Exception e) { }
			}
		}
		return resultDatas;
	}

	/**
	 *  生成密钥对(公钥和私钥)
	 * @return
	 * @throws Exception
	 */
	public static KeyPair genKeyPair(int keyLength) throws Exception {
		KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(ALGORITHM);
		keyPairGen.initialize(keyLength);

		return keyPairGen.generateKeyPair();
	}

	/**
	 * 获取私钥
	 * @param keyPair
	 * @return
	 * @throws Exception
	 */
	public static String getPrivateKey(KeyPair keyPair) throws Exception {
		Key key = keyPair.getPrivate();

		BASE64Encoder encoder = new BASE64Encoder();
		return encoder.encode(key.getEncoded());
	}

	/**
	 * 获取公钥
	 * @param keyPair
	 * @return
	 * @throws Exception
	 */
	public static String getPublicKey(KeyPair keyPair) throws Exception {
		Key key = keyPair.getPublic();

		BASE64Encoder encoder = new BASE64Encoder();
		return encoder.encode(key.getEncoded());
	}

	/**
	 * 私钥提取公钥
	 * @param privateKey
	 * @return
	 * @throws Exception
	 */
	public static String extractPublicKey(String privateKey) throws Exception {
		KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);

		BASE64Decoder base64Decoder = new BASE64Decoder();
		byte[] keyBytes = base64Decoder.decodeBuffer(privateKey);
		Key key = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(keyBytes));

		RSAPrivateKeySpec priv = keyFactory.getKeySpec(key, RSAPrivateKeySpec.class);
		RSAPublicKeySpec keySpec = new RSAPublicKeySpec(priv.getModulus(), BigInteger.valueOf(65537));

		PublicKey publicKey = keyFactory.generatePublic(keySpec);

		BASE64Encoder encoder = new BASE64Encoder();
		return encoder.encode(publicKey.getEncoded());
	}

	/**
	 * 私钥生成
	 * @param privateKey
	 * @return
	 * @throws Exception
	 */
	public static Key getPrivateKey(String privateKey) throws Exception {
		BASE64Decoder base64Decoder = new BASE64Decoder();
		byte[] keyBytes = base64Decoder.decodeBuffer(privateKey);

		KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);

		return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(keyBytes));
	}

	/**
	 * 公钥
	 * @param publicKey
	 * @return
	 * @throws Exception
	 */
	public static Key getPublicKey(String publicKey) throws Exception {
		BASE64Decoder base64Decoder = new BASE64Decoder();
		byte[] keyBytes = base64Decoder.decodeBuffer(publicKey);

		KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);

		return keyFactory.generatePublic(new X509EncodedKeySpec(keyBytes));
	}


	public static KeyStore getKeystore(InputStream fis, String pwd) throws Exception {
		//InputStream fis = new ByteArrayInputStream(new Base64().decode(privateKey));
		KeyStore ks = KeyStore.getInstance("PKCS12");
		ks.load(fis, pwd.toCharArray());
		fis.close();

		return ks;
	}

	public static Key getPrivateKeyFromFile(String filePath, String pwd) throws Exception {
		//ClassPathResource resource = new ClassPathResource(filePath);
		File file = new File(filePath);
		InputStream fis = new FileInputStream(file);
		KeyStore ks = getKeystore(fis, pwd);

		Enumeration<String> enumas = ks.aliases();
		String keyAlias = null;
		if (enumas.hasMoreElements()) {
			keyAlias = enumas.nextElement();
		}

		return ks.getKey(keyAlias, pwd.toCharArray());
	}

	public static Key getPrivateKey(String keyStoreStr, String pwd) throws Exception {
		InputStream fis = new ByteArrayInputStream(new Base64().decode(keyStoreStr));
		KeyStore ks = getKeystore(fis, pwd);

		Enumeration<String> enumas = ks.aliases();
		String keyAlias = null;
		if (enumas.hasMoreElements()) {
			keyAlias = enumas.nextElement();
		}

		return ks.getKey(keyAlias, pwd.toCharArray());
	}

	public static Key getPublicKeyFromFile(String filePath, String pwd) throws Exception {
		//ClassPathResource resource = new ClassPathResource(filePath);
		File file = new File(filePath);
		InputStream fis = new FileInputStream(file);
		KeyStore ks = getKeystore(fis, pwd);

		Enumeration<String> enumas = ks.aliases();
		String keyAlias = null;
		if (enumas.hasMoreElements()) {
			keyAlias = enumas.nextElement();
		}

		Certificate cert = ks.getCertificate(keyAlias);
		return cert.getPublicKey();
	}

	public static Key getPublicKey(String keyStoreStr, String pwd) throws Exception {
		InputStream fis = new ByteArrayInputStream(new Base64().decode(keyStoreStr));
		KeyStore ks = getKeystore(fis, pwd);

		Enumeration<String> enumas = ks.aliases();
		String keyAlias = null;
		if (enumas.hasMoreElements()) {
			keyAlias = enumas.nextElement();
		}

		Certificate cert = ks.getCertificate(keyAlias);
		return cert.getPublicKey();
	}


	public static void main(String[] args) throws Exception {
		int keyLength = 2048;
		KeyPair keyPair = genKeyPair(keyLength);
		String privateKey = getPrivateKey(keyPair);
		String publicKey = getPublicKey(keyPair);

		/*System.out.println(privateKey);
		System.out.println("==========================");
		System.out.println(publicKey);*/

		String info = "hello, i'm lord melon";
		String encrypted = encrypt(info, publicKey, keyLength);
		System.out.println(encrypted);

		String decrypted = decrypt(encrypted, privateKey, keyLength);
		System.out.println(decrypted);
	}

}
