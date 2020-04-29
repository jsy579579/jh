package com.jh.paymentgateway.util.tyt;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Hex;

public class MyEncryptUtils {
	public static byte[] encrypt(String content, String encKey) {  
        try {             
                Cipher cipher = Cipher.getInstance("AES");  
                SecretKeySpec secretKey = new SecretKeySpec(encKey.getBytes(), "AES");  
                byte[] byteContent = content.getBytes();  
                cipher.init(Cipher.ENCRYPT_MODE, secretKey);  
                byte[] result = cipher.doFinal(byteContent);  
                return result;   
        } catch (NoSuchAlgorithmException e) {  
                e.printStackTrace();  
        } catch (NoSuchPaddingException e) {  
                e.printStackTrace();  
        } catch (InvalidKeyException e) {  
                e.printStackTrace();  
        } catch (IllegalBlockSizeException e) {  
                e.printStackTrace();  
        } catch (BadPaddingException e) {  
                e.printStackTrace();  
        } catch (Exception e) {  
            	e.printStackTrace();  
        }   
        return null;  
	}  
	public static String byte2hex(byte[] input) {
	    return Hex.encodeHexString(input);
	}
}
