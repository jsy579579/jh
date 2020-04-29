package com.jh.paymentgateway.util.jftx;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.X509Certificate;


public class RSA {
	
	private static final String signAlgorithm = "SHA256WithRSA";
	private static final String cipherAlgorithm = "RSA/ECB/PKCS1Padding";
	
	private static final int keyLength=2048;
	private static final int  reserveSize =11;
	
	
    public static byte[] publicKeyEncrypt(String plainText, PublicKey publicKey){
		
		return publicKeyEncrypt(plainText, publicKey,  keyLength,  reserveSize);
		
	}
	
	public static byte[] publicKeyEncrypt(String plainText, PublicKey publicKey, int keyLength, int reserveSize){
		
		return publicKeyEncrypt(getUTF8Bytes(plainText), publicKey,  keyLength,  reserveSize,  cipherAlgorithm);
		
	}
	
	/**
	 * RSA加密
	 * 
	 * @param plainBytes
	 *            明文字节数组
	 * @param publicKey
	 *            公钥
	 * @param keyLength
	 *            密钥bit长度
	 * @param reserveSize
	 *            padding填充字节数，预留11字节
	 * @param cipherAlgorithm
	 *            加解密算法，一般为RSA/ECB/PKCS1Padding
	 * @return 加密后字节数组，不经base64编码
	 * @throws RuntimeException
	 */
	public static byte[] publicKeyEncrypt(byte[] plainBytes, PublicKey publicKey, int keyLength, int reserveSize, String cipherAlgorithm)
			{
		int keyByteSize = keyLength / 8; // 密钥字节数
		int encryptBlockSize = keyByteSize - reserveSize; // 加密块大小=密钥字节数-padding填充字节数
		int nBlock = plainBytes.length / encryptBlockSize;// 计算分段加密的block数，向上取整
		if ((plainBytes.length % encryptBlockSize) != 0) { // 余数非0，block数再加1
			nBlock += 1;
		}

		try {
			Cipher cipher = Cipher.getInstance(cipherAlgorithm);
			cipher.init(Cipher.ENCRYPT_MODE, publicKey);

			// 输出buffer，大小为nBlock个keyByteSize
			ByteArrayOutputStream outbuf = new ByteArrayOutputStream(nBlock * keyByteSize);
			// 分段加密
			for (int offset = 0; offset < plainBytes.length; offset += encryptBlockSize) {
				int inputLen = plainBytes.length - offset;
				if (inputLen > encryptBlockSize) {
					inputLen = encryptBlockSize;
				}

				// 得到分段加密结果
				byte[] encryptedBlock = cipher.doFinal(plainBytes, offset, inputLen);
				// 追加结果到输出buffer中
				outbuf.write(encryptedBlock);
			}

			outbuf.flush();
			outbuf.close();
			return outbuf.toByteArray();
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(String.format("没有[%s]此类加密算法", cipherAlgorithm),e);
		} catch (NoSuchPaddingException e) {
			throw new RuntimeException(String.format("没有[%s]此类填充模式", cipherAlgorithm),e);
		} catch (InvalidKeyException e) {
			throw new RuntimeException("无效密钥",e);
		} catch (IllegalBlockSizeException e) {
			throw new RuntimeException("加密块大小不合法",e);
		} catch (BadPaddingException e) {
			throw new RuntimeException("错误填充模式",e);
		} catch (IOException e) {
			throw new RuntimeException("字节输出流异常",e);
		}
	}
	
	
	public static String privateKeyDecrypt(byte[] encryptedBytes, PrivateKey privateKey)
	 {
		return privateKeyDecrypt( encryptedBytes,  privateKey,  keyLength,  reserveSize);
	 }
	
	public static String privateKeyDecrypt(byte[] encryptedBytes, PrivateKey privateKey, int keyLength, int reserveSize)
	 {
		return new String(privateKeyDecrypt( encryptedBytes,  privateKey,  keyLength,  reserveSize,  cipherAlgorithm) ,StandardCharsets.UTF_8);
	 }

	/**
	 * RSA解密
	 * 
	 * @param encryptedBytes
	 *            加密后字节数组
	 * @param privateKey
	 *            私钥
	 * @param keyLength
	 *            密钥bit长度
	 * @param reserveSize
	 *            padding填充字节数，预留11字节
	 * @param cipherAlgorithm
	 *            加解密算法，一般为RSA/ECB/PKCS1Padding
	 * @return 解密后字节数组，不经base64编码
	 * @throws RuntimeException
	 */
	public static byte[] privateKeyDecrypt(byte[] encryptedBytes, PrivateKey privateKey, int keyLength, int reserveSize, String cipherAlgorithm)
			 {
		int keyByteSize = keyLength / 8; // 密钥字节数
		int decryptBlockSize = keyByteSize - reserveSize; // 解密块大小=密钥字节数-padding填充字节数
		int nBlock = encryptedBytes.length / keyByteSize;// 计算分段解密的block数，理论上能整除

		try {
			Cipher cipher = Cipher.getInstance(cipherAlgorithm);
			cipher.init(Cipher.DECRYPT_MODE, privateKey);

			// 输出buffer，大小为nBlock个decryptBlockSize
			ByteArrayOutputStream outbuf = new ByteArrayOutputStream(nBlock * decryptBlockSize);
			// 分段解密
			for (int offset = 0; offset < encryptedBytes.length; offset += keyByteSize) {
				// block大小: decryptBlock 或 剩余字节数
				int inputLen = encryptedBytes.length - offset;
				if (inputLen > keyByteSize) {
					inputLen = keyByteSize;
				}

				// 得到分段解密结果
				byte[] decryptedBlock = cipher.doFinal(encryptedBytes, offset, inputLen);
				// 追加结果到输出buffer中
				outbuf.write(decryptedBlock);
			}

			outbuf.flush();
			outbuf.close();
			return outbuf.toByteArray();
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(String.format("没有[%s]此类解密算法", cipherAlgorithm),e);
		} catch (NoSuchPaddingException e) {
			throw new RuntimeException(String.format("没有[%s]此类填充模式", cipherAlgorithm),e);
		} catch (InvalidKeyException e) {
			throw new RuntimeException("无效密钥",e);
		} catch (IllegalBlockSizeException e) {
			throw new RuntimeException("解密块大小不合法",e);
		} catch (BadPaddingException e) {
			throw new RuntimeException("错误填充模式",e);
		} catch (IOException e) {
			throw new RuntimeException("字节输出流异常",e);
		}
	}
	
	
	
	public static  byte[] digitalPrivateKeySign(String plainText, PrivateKey privateKey) {
		byte[] plainBytes = getUTF8Bytes(plainText);
		return digitalPrivateKeySign(plainBytes, privateKey, signAlgorithm);
	}
	
	
	/**
	 * 数字签名函数入口
	 * 
	 * @param plainBytes
	 *            待签名明文字节数组
	 * @param privateKey
	 *            签名使用私钥
	 * @param signAlgorithm
	 *            签名算法
	 * @return 签名后的字节数组
	 * @throws RuntimeException
	 */
	public static byte[] digitalPrivateKeySign(byte[] plainBytes, PrivateKey privateKey, String signAlgorithm)  {
		try {
			Signature signature = Signature.getInstance(signAlgorithm);
			signature.initSign(privateKey);
			signature.update(plainBytes);
			byte[] signBytes = signature.sign();

			return signBytes;
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(String.format("数字签名时没有[%s]此类算法", signAlgorithm),e);
		} catch (InvalidKeyException e) {
			throw new RuntimeException("数字签名时私钥无效",e);
		} catch (SignatureException e) {
			throw new RuntimeException("数字签名时出现异常",e);
		}
	}

	 public static boolean verifyPublicKeyDigitalSign(String plainText, byte []  signBytes, PublicKey publicKey)  {
		byte [] plainBytes =  getUTF8Bytes(plainText);
		return verifyPublicKeyDigitalSign(plainBytes,  signBytes,  publicKey,  signAlgorithm) ;
	}
	
	 public static boolean verifyPublicKeyDigitalSign(byte [] plainBytes, byte []  signBytes, PublicKey publicKey)  {
			
			return verifyPublicKeyDigitalSign(plainBytes,  signBytes,  publicKey,  signAlgorithm) ;
		}
	 
	/**
	 * 验证数字签名函数入口
	 * 
	 * @param plainBytes
	 *            待验签明文字节数组
	 * @param signBytes
	 *            待验签签名后字节数组
	 * @param publicKey
	 *            验签使用公钥
	 * @param signAlgorithm
	 *            签名算法
	 * @return 验签是否通过
	 * @throws RuntimeException
	 */
	public static boolean verifyPublicKeyDigitalSign(byte[] plainBytes, byte[] signBytes, PublicKey publicKey, String signAlgorithm)  {
		boolean isValid = false;
		try {
			Signature signature = Signature.getInstance(signAlgorithm);
			signature.initVerify(publicKey);
			signature.update(plainBytes);
			isValid = signature.verify(signBytes);
			return isValid;
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(String.format("验证数字签名时没有[%s]此类算法", signAlgorithm),e);
		} catch (InvalidKeyException e) {
			throw new RuntimeException("验证数字签名时公钥无效",e);
		} catch (SignatureException e) {
			throw new RuntimeException("验证数字签名时出现异常",e);
		}
	}

	/**
	 * 验证数字签名函数入口
	 * 
	 * @param plainBytes
	 *            待验签明文字节数组
	 * @param signBytes
	 *            待验签签名后字节数组
	 * @param cert
	 *            验签使用公钥
	 * @param signAlgorithm
	 *            签名算法
	 * @return 验签是否通过
	 * @throws RuntimeException
	 */
	@Deprecated
	public static boolean verifyPublicKeyDigitalSign(byte[] plainBytes, byte[] signBytes, X509Certificate cert, String signAlgorithm)   {
		boolean isValid = false;
		try {
			Signature signature = Signature.getInstance(signAlgorithm);
			signature.initVerify(cert);
			signature.update(plainBytes);
			isValid = signature.verify(signBytes);
			return isValid;
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(String.format("验证数字签名时没有[%s]此类算法", signAlgorithm),e);
		} catch (InvalidKeyException e) {
			throw new RuntimeException("验证数字签名时公钥无效",e);
		} catch (SignatureException e) {
			throw new RuntimeException("验证数字签名时出现异常",e);
		}
	}
	
	
	private static byte[] getUTF8Bytes(String input) {
		return input.getBytes(StandardCharsets.UTF_8);
	}
	


}
