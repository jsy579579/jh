package com.jh.paymentgateway.util.ap.security;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.function.Function;

/**
 * @Description: AES加密解密算法
 * @author: dengzhixin
 * @date 2017年3月22日 上午11:36:37
 *
 */
public class AESUtil {

	/**
	 * 加密算法
	 */
	private static final String ALGORITHM = "AES";

	/**
	 * 默认编码方式
	 */
	private static final String DEFAULT_CHARSET = "utf-8";
	
	private static Function<String, byte[]> pre = (str) -> {try{ return str.getBytes(DEFAULT_CHARSET);} catch(Exception e){} return null;};
	private static Function<byte[], String> suf = (byt) -> {try{return new String(byt,DEFAULT_CHARSET);} catch(Exception e){} return null;};

	private static Function<String, byte[]> pre64 = (str) -> { try { return new BASE64Decoder().decodeBuffer(str); } catch (Exception e) { } return null; };
	private static Function<byte[], String> suf64 = (byt) -> new BASE64Encoder().encodeBuffer(byt);

	private static Function<byte[], String> sufHex = (byt) -> Hex.encodeHexString(byt);
	private static Function<String, byte[]> preHex = (str) -> { try { return Hex.decodeHex(str.toCharArray()); } catch (Exception e) { } return null; };

	//===========================================ECB========================================
	public static String base64EcbEncrypt(String content, String encKey) throws Exception {
		return encrypt(content, encKey, null, pre, suf64, PaddingMode.AES_ECB);
	}

	public static String base64EcbDecrypt(String content, String encKey) throws Exception {
		return decrypt(content, encKey, null, pre64, suf, PaddingMode.AES_ECB);
	}

	public static String ecbEncrypt(String content, String encKey) throws Exception {
		return encrypt(content, encKey, null, pre, sufHex, PaddingMode.AES_ECB);
	}

	public static String ecbDecrypt(String content, String encKey) throws Exception {
		return decrypt(content, encKey, null, preHex, suf, PaddingMode.AES_ECB);
	}
	//============================================ECB=======================================

	//===========================================CBC========================================
	public static String base64CbcEncrypt(String content, String encKey) throws Exception {
		return encrypt(content, encKey, encKey, pre, suf64, PaddingMode.AES_CBC);
	}

	public static String base64CbcDecrypt(String content, String encKey) throws Exception {
		return decrypt(content, encKey, encKey, pre64, suf, PaddingMode.AES_CBC);
	}
	
	public static String cbcEncrypt(String content, String encKey) throws Exception {
		return encrypt(content, encKey, encKey, pre, sufHex, PaddingMode.AES_CBC);
	}

	public static String cbcDecrypt(String content, String encKey) throws Exception {
		return decrypt(content, encKey, encKey, preHex, suf, PaddingMode.AES_CBC);
	}

	//============================================CBC=======================================

	/**
	 * encrypt with random seed
	 * @param content
	 * @param seed
	 * @param pre
	 * @param suf
	 * @param paddingMode
	 * @return
	 * @throws Exception
	 */
	private static String seedEncrypt(String content, String seed, Function<String, byte[]> pre, Function<byte[], String> suf, PaddingMode paddingMode)
			throws Exception {
		byte[] data = pre.apply(content);
		byte[] key = genKeyWithSeed(seed);
		byte[] ivKey = getIvKey(key, paddingMode);
		byte[] encrypted = process(data, key, ivKey, Cipher.ENCRYPT_MODE, paddingMode);
		return suf.apply(encrypted);
	}

	/**
	 * decrypt with random seed
	 * @param content
	 * @param seed
	 * @param pre
	 * @param suf
	 * @param paddingMode
	 * @return
	 * @throws Exception
	 */
	private static String seedDecrypt(String content, String seed, Function<String, byte[]> pre, Function<byte[], String> suf, PaddingMode paddingMode)
			throws Exception {
		byte[] data = pre.apply(content);
		byte[] key = genKeyWithSeed(seed);
		byte[] ivKey = getIvKey(key, paddingMode);
		byte[] encrypted = process(data, key, ivKey, Cipher.DECRYPT_MODE, paddingMode);
		return suf.apply(encrypted);
	}

	/**
	 * encrypt with key
	 * @param content
	 * @param encKey
	 * @param iv
	 * @param pre
	 * @param suf
	 * @param paddingMode
	 * @return
	 * @throws Exception
	 */
	private static String encrypt(String content, String encKey, String iv, Function<String, byte[]> pre, Function<byte[], String> suf, PaddingMode paddingMode)
			throws Exception {
//		byte[] data = pre.apply(content);
		byte[] data = content.getBytes(DEFAULT_CHARSET);
		byte[] key = encKey.getBytes(DEFAULT_CHARSET);
		byte[] ivKey = StringUtils.isNotBlank(iv) ? getIvKey(iv.getBytes(), paddingMode) : null;

		byte[] encrypted = process(data, key, ivKey, Cipher.ENCRYPT_MODE, paddingMode);
		return suf.apply(encrypted);
	}

	/**
	 * decrypt with key
	 * @param content
	 * @param encKey
	 * @param iv
	 * @param pre
	 * @param suf
	 * @param paddingMode
	 * @return
	 * @throws Exception
	 */
	private static String decrypt(String content, String encKey, String iv, Function<String, byte[]> pre, Function<byte[], String> suf, PaddingMode paddingMode)
			throws Exception {
		byte[] data = pre.apply(content);
		byte[] key = encKey.getBytes(DEFAULT_CHARSET);
		byte[] ivKey = StringUtils.isNotBlank(iv) ? getIvKey(iv.getBytes(), paddingMode) : null;
		byte[] encrypted = process(data, key, ivKey, Cipher.DECRYPT_MODE, paddingMode);
		return suf.apply(encrypted);
	}

	/**
	 * main encrypt/decrypt process
	 * @param data
	 * @param key
	 * @param oprMode
	 * @param paddingMode
	 * @return
	 * @throws Exception
	 */
	private static byte[] process(byte[] data, byte[] key, byte[] spec, int oprMode, PaddingMode paddingMode) throws Exception {
		if(null == key || (key.length != 16 && key.length != 24 && key.length != 32)){
			throw new Exception("key长度必须是16或者24或者32");
		}

		SecretKeySpec keySpec = new SecretKeySpec(key, ALGORITHM);

		IvParameterSpec iv = null;
		if(null != spec && spec.length > 0){
			iv = new IvParameterSpec(spec);
		}

		Cipher cipher = Cipher.getInstance(paddingMode.getType());
		cipher.init(oprMode, keySpec, iv);
		return cipher.doFinal(data);
	}

	private static byte[] getIvKey(byte[] iv, PaddingMode paddingMode){
		byte[] ivKey = null;
		if(paddingMode == PaddingMode.AES_CBC && null != iv && iv.length >= 16){
			ivKey = new byte[16];
			System.arraycopy(iv, 0, ivKey, 0, 16);
		}
		return ivKey;
	}

	/**
	 * generate key with seed
	 * @param seed
	 * @return
	 * @throws Exception
	 */
	private static byte[] genKeyWithSeed(String seed) throws Exception {
		KeyGenerator kg = KeyGenerator.getInstance(ALGORITHM);
		kg.init(256, new SecureRandom(seed.getBytes()));

		SecretKey secretKey = kg.generateKey();
		return secretKey.getEncoded();
	}

	public static void main(String[] args) throws Exception {

		String sKey = "57f6f9d45edc2";

		// 需要加密的字串
		String content = "hello,i'm Lord Melon";
		System.out.println("加密前的字串是：" + content);

		/*String enString = AESUtil.getInstance().base64CbcEncrypt(content, sKey);
		System.out.println("加密后的字串是：" + enString);

		String deString = AESUtil.getInstance().base64CbcDecrypt(enString, sKey);
		System.out.println("解密后的字串是：" + deString);*/

//		sKey = "abcdefghijklmnop";
		sKey = "hUlVS82EbtKXp30jgj0QhV35";

		//ecb
		/*String enString = AESUtil.base64EcbEncrypt(content, sKey);
		System.out.println("加密后的字串是：" + enString);

		String deString = AESUtil.base64EcbDecrypt(enString, sKey);
		System.out.println("解密后的字串是：" + deString);*/

		/*String enString = AESUtil.ecbEncrypt(content, sKey);
		System.out.println("加密后的字串是：" + enString);

		String deString = AESUtil.ecbDecrypt(enString, sKey);
		System.out.println("解密后的字串是：" + deString);*/

		//cbc
		/*String enString = AESUtil.base64CbcEncrypt(content, sKey);
		System.out.println("加密后的字串是：" + enString);

		String deString = AESUtil.base64CbcDecrypt(enString, sKey);
		System.out.println("解密后的字串是：" + deString);*/

		/*String enString = AESUtil.cbcEncrypt(content, sKey);
		System.out.println("加密后的字串是：" + enString);

		String deString = AESUtil.cbcDecrypt(enString, sKey);
		System.out.println("解密后的字串是：" + deString);*/
		
		String seed = "aaaaa";
		/*String enString = seedEncrypt(content, seed, pre, sufHex, PaddingMode.AES_CBC);
		System.out.println("加密后的字串是：" + enString);

		String deString = seedDecrypt(enString, seed, preHex, suf, PaddingMode.AES_CBC);
		System.out.println("解密后的字串是：" + deString);*/

		/*String enString = seedEncrypt(content, seed, pre, sufHex, PaddingMode.AES_ECB);
		System.out.println("加密后的字串是：" + enString);

		String deString = seedDecrypt(enString, seed, preHex, suf, PaddingMode.AES_ECB);
		System.out.println("解密后的字串是：" + deString);*/
	}
}
