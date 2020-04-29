package com.jh.paymentchannel.util;



import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.security.Key;
import java.security.KeyFactory;
import java.security.spec.PKCS8EncodedKeySpec;

import javax.crypto.Cipher;


public final class RSAUtils {
	
	/**

	 */
	private static final String KEY_ALGORITHM = "RSA";

	/**

	 */
	private static final int MAX_ENCRYPT_BLOCK = 117;

	/**
	 */
	private static final int MAX_DECRYPT_BLOCK = 128;
	
	/**
     * <P>
     * </p>
     *
 
     * @return
     */
    public static String decryptByPrivateKey(String encryptedContext, String filepath)
            throws Exception {
        byte[] keyBytes = Base64Utils.decode(readPrivateKey(filepath));
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        Key privateK = keyFactory.generatePrivate(pkcs8KeySpec);
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.DECRYPT_MODE, privateK);
        byte[] bytes = encryptedContext.getBytes();  
        byte[] encryptedData = ASCII_To_BCD(bytes, bytes.length);
        int inputLen = encryptedData.length;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offSet = 0;
        byte[] cache;
        int i = 0;
        // �����ݷֶν���
        while (inputLen - offSet > 0) {
            if (inputLen - offSet > MAX_DECRYPT_BLOCK) {
                cache = cipher.doFinal(encryptedData, offSet, MAX_DECRYPT_BLOCK);
            } else {
                cache = cipher.doFinal(encryptedData, offSet, inputLen - offSet);
            }
            out.write(cache, 0, cache.length);
            i++;
            offSet = i * MAX_DECRYPT_BLOCK;
        }
        byte[] decryptedData = out.toByteArray();
        out.close();
        return new String(decryptedData);
    }


    /**
     * <p>

     * </p>
     *

     * @return
     * @throws Exception
     */
    public static String encryptByPrivateKey(String encryptedContext,  String filepath)
            throws Exception {
        byte[] keyBytes = Base64Utils.decode(readPrivateKey(filepath));
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        Key privateK = keyFactory.generatePrivate(pkcs8KeySpec);
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.ENCRYPT_MODE, privateK);
        byte[] data = encryptedContext.getBytes();
        int inputLen = data.length;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offSet = 0;
        byte[] cache;
        int i = 0;
       
        while (inputLen - offSet > 0) {
            if (inputLen - offSet > MAX_ENCRYPT_BLOCK) {
                cache = cipher.doFinal(data, offSet, MAX_ENCRYPT_BLOCK);
            } else {
                cache = cipher.doFinal(data, offSet, inputLen - offSet);
            }
            out.write(cache, 0, cache.length);
            i++;
            offSet = i * MAX_ENCRYPT_BLOCK;
        }
        byte[] encryptedData = out.toByteArray();
        out.close();
        return  bcd2Str(encryptedData);
    }
    
    /**
	 */
	public static String bcd2Str(byte[] bytes) {
		char temp[] = new char[bytes.length * 2], val;

		for (int i = 0; i < bytes.length; i++) {
			val = (char) (((bytes[i] & 0xf0) >> 4) & 0x0f);
			temp[i * 2] = (char) (val > 9 ? val + 'A' - 10 : val + '0');

			val = (char) (bytes[i] & 0x0f);
			temp[i * 2 + 1] = (char) (val > 9 ? val + 'A' - 10 : val + '0');
		}
		return new String(temp);
	}
	
	/**
	 * 
	 */
	public static byte[] ASCII_To_BCD(byte[] ascii, int asc_len) {
		byte[] bcd = new byte[asc_len / 2];
		int j = 0;
		for (int i = 0; i < (asc_len + 1) / 2; i++) {
			bcd[i] = asc_to_bcd(ascii[j++]);
			bcd[i] = (byte) (((j >= asc_len) ? 0x00 : asc_to_bcd(ascii[j++])) + (bcd[i] << 4));
		}
		return bcd;
	}

	public static byte asc_to_bcd(byte asc) {
		byte bcd;

		if ((asc >= '0') && (asc <= '9'))
			bcd = (byte) (asc - '0');
		else if ((asc >= 'A') && (asc <= 'F'))
			bcd = (byte) (asc - 'A' + 10);
		else if ((asc >= 'a') && (asc <= 'f'))
			bcd = (byte) (asc - 'a' + 10);
		else
			bcd = (byte) (asc - 48);
		return bcd;
	}
	
	private static String readPrivateKey(String filePath) throws Exception {
		String privateKey = "";
		Reader reader = null;
		try {
			File inFile = new File(filePath);
			long fileLen = inFile.length();
			reader = new FileReader(inFile);
			char[] content = new char[(int) fileLen];
			reader.read(content);
			privateKey = new String(content);
			privateKey = privateKey.replace("-----BEGIN PRIVATE KEY-----", "");
			privateKey = privateKey.replace("-----END PRIVATE KEY-----", "");
			
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			if( null != reader )
			{
				reader.close();
			}
		}
		return privateKey;
	}
}
