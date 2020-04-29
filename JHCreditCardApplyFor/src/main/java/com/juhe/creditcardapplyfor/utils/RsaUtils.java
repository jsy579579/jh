package com.juhe.creditcardapplyfor.utils;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Base64Utils;

import javax.crypto.Cipher;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

/**
 * RSA算法，实现数据的加密解密。
 *
 * @author Peter
 */
public class RsaUtils {

    private static Cipher cipher;

    private static final String RSA_ALGORITHM = "RSA";

    private static final String RSA_SIGNATURE_1_ALGORITHM = "SHA1withRSA";
    public static final String RSA_SIGNATURE_256_ALGORITHM = "SHA256WithRSA";

    static {
        try {
            cipher = Cipher.getInstance(RSA_ALGORITHM);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Map<String, String> createKeys(int keySize) throws Exception {
        // 为RSA算法创建一个KeyPairGenerator对象
        KeyPairGenerator kpg;
        try {
            kpg = KeyPairGenerator.getInstance(RSA_ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException("No such algorithm-->[" + RSA_ALGORITHM + "]");
        }

        // 初始化KeyPairGenerator对象,密钥长度
        kpg.initialize(keySize);

        // 生成密匙对
        KeyPair keyPair = kpg.generateKeyPair();

        // 得到公钥
        Key publicKey = keyPair.getPublic();
        String publicKeyStr = new String(Base64Utils.encodeToString(publicKey.getEncoded()));

        // 得到私钥
        Key privateKey = keyPair.getPrivate();
        String privateKeyStr = new String(Base64Utils.encodeToString(privateKey.getEncoded()));

        Map<String, String> keyPairMap = new HashMap<String, String>();
        keyPairMap.put("publicKey", publicKeyStr);
        keyPairMap.put("privateKey", privateKeyStr);

        return keyPairMap;
    }

    /**
     * 得到公钥
     *
     * @param key 密钥字符串（经过base64编码）
     * @throws Exception
     */
    private static PublicKey getPublicKey(String key) throws Exception {
        byte[] keyBytes = Base64Utils.decodeFromString(key);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(keySpec);
    }

    /**
     * 得到私钥
     *
     * @param key 密钥字符串（经过base64编码）
     * @throws Exception
     */
    private static PrivateKey getPrivateKey(String key) throws Exception {
        byte[] keyBytes = Base64Utils.decodeFromString(key);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(keySpec);
    }

    /**
     * 得到密钥字符串（经过base64编码）
     *
     * @return
     */
    public static String getKeyString(Key key) throws Exception {
        byte[] keyBytes = key.getEncoded();
        return Base64Utils.encodeToString(keyBytes);
    }

    /**
     * 使用公钥对明文进行加密，返回BASE64编码的字符串
     *
     * @param publicKey
     * @param plainText
     * @return
     */
    public static String encrypt(PublicKey publicKey, String plainText) {
        try {
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] enBytes = cipher.doFinal(plainText.getBytes());
            return Base64Utils.encodeToString(enBytes);
        } catch (Exception e) {
            return StringUtils.EMPTY;
        }
    }

    /**
     * 使用公钥对明文进行加密
     *
     * @param publicKey 公钥
     * @param plainText 明文
     * @return
     */
    public static String encrypt(String publicKey, String plainText) {
        try {
            cipher.init(Cipher.ENCRYPT_MODE, getPublicKey(publicKey));
            byte[] enBytes = cipher.doFinal(plainText.getBytes());
            return Base64Utils.encodeToString(enBytes);
        } catch (Exception e) {
            return StringUtils.EMPTY;
        }
    }

    /**
     * 使用私钥对明文密文进行解密
     *
     * @param privateKey
     * @param enStr
     * @return
     */
    public static String decrypt(PrivateKey privateKey, String enStr) {
        try {
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] deBytes = cipher.doFinal(Base64Utils.decodeFromString(enStr));
            return new String(deBytes);
        } catch (Exception e) {
            return StringUtils.EMPTY;
        }
    }

    /**
     * 使用私钥对密文进行解密
     *
     * @param privateKey 私钥
     * @param enStr      密文
     * @return
     */
    public static String decrypt(String privateKey, String enStr) {
        try {
            cipher.init(Cipher.DECRYPT_MODE, getPrivateKey(privateKey));
            byte[] deBytes = cipher.doFinal(Base64Utils.decodeFromString(enStr));
            return new String(deBytes);
        } catch (Exception e) {
            return StringUtils.EMPTY;
        }
    }

    /**
     * 私钥加密, 需要用公钥解密
     *
     * @param privateKey
     * @param plainText
     * @return
     */
    public static String encryptByPrivateKey(String privateKey, String plainText) {
        try {
            cipher.init(Cipher.ENCRYPT_MODE, getPrivateKey(privateKey));
            byte[] enBytes = cipher.doFinal(plainText.getBytes());
            return Base64Utils.encodeToString(enBytes);
        } catch (Exception e) {
            return StringUtils.EMPTY;
        }
    }

    /**
     * 公钥解密，需要用私钥加密
     *
     * @param publicKey
     * @param enStr
     * @return
     */
    public static String decryptByPublicKey(String publicKey, String enStr) {
        try {
            cipher.init(Cipher.DECRYPT_MODE, getPublicKey(publicKey));
            byte[] deBytes = cipher.doFinal(Base64Utils.decodeFromString(enStr));
            return new String(deBytes);
        } catch (Exception e) {
            return StringUtils.EMPTY;
        }
    }

    /**
     * 私钥签名
     *
     * @param privateKeyBase64
     * @param data
     * @return
     * @throws Exception
     */
    public static String sign(String privateKeyBase64, String data) {
        return sign(privateKeyBase64, data, RSA_SIGNATURE_1_ALGORITHM);
    }

    /**
     * 私钥签名
     *
     * @param privateKeyBase64
     * @param data
     * @param signAlgorithm
     * @return
     * @throws Exception
     */
    private static String sign(String privateKeyBase64, String data, String signAlgorithm) {
        try {
            PrivateKey privateKey = getPrivateKey(privateKeyBase64);
            Signature signature = Signature.getInstance(signAlgorithm);
            signature.initSign(privateKey);
            signature.update(data.getBytes());
            return Base64Utils.encodeToString(signature.sign());
        } catch (Exception ex) {
            return StringUtils.EMPTY;
        }
    }

    /**
     * 公钥验签
     *
     * @param data
     * @param publicKeyBase64
     * @param sign
     * @return
     * @throws Exception
     */
    public static boolean verify(String publicKeyBase64, String data, String sign) {
        return verify(publicKeyBase64, data, sign, RSA_SIGNATURE_1_ALGORITHM);
    }

    /**
     * 公钥验签
     *
     * @param data
     * @param publicKeyBase64
     * @param sign
     * @return
     * @throws Exception
     */
    private static boolean verify(String publicKeyBase64, String data, String sign, String signAlgorithm) {
        try {
            PublicKey publicKey = getPublicKey(publicKeyBase64);
            Signature signature = Signature.getInstance(signAlgorithm);
            signature.initVerify(publicKey);
            signature.update(data.getBytes());
            return signature.verify(Base64Utils.decodeFromUrlSafeString(sign));
        } catch (Exception ex) {
            return false;
        }
    }

}