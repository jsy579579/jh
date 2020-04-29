package com.jh.paymentgateway.controller.yxe.util;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;


/*
 * AES对称加密和解密
 */
public class AesUtil {

    /*
     * 加密
     * 1.构造密钥生成器
     * 2.根据ecnodeRules规则初始化密钥生成器
     * 3.产生密钥
     * 4.创建和初始化密码器
     * 5.内容加密
     * 6.返回字符串
     */
    public static String AESEncode(String encodeRules, String content){
        try {
            //1.构造密钥生成器，指定为AES算法,不区分大小写
            KeyGenerator keygen= KeyGenerator.getInstance("AES");
            //2.根据ecnodeRules规则初始化密钥生成器
            //生成一个128位的随机源,根据传入的字节数组
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            random.setSeed(encodeRules.getBytes());
            keygen.init(128, random);
            //3.产生原始对称密钥
            SecretKey original_key=keygen.generateKey();
            //4.获得原始对称密钥的字节数组
            byte [] raw=original_key.getEncoded();
            //5.根据字节数组生成AES密钥
            SecretKey key=new SecretKeySpec(raw, "AES");
            //6.根据指定算法AES自成密码器
            Cipher cipher= Cipher.getInstance("AES");
            //7.初始化密码器，第一个参数为加密(Encrypt_mode)或者解密解密(Decrypt_mode)操作，第二个参数为使用的KEY
            cipher.init(Cipher.ENCRYPT_MODE, key);
            //8.获取加密内容的字节数组(这里要设置为utf-8)不然内容中如果有中文和英文混合中文就会解密为乱码
            byte [] byte_encode=content.getBytes("utf-8");
            //9.根据密码器的初始化方式--加密：将数据加密
            byte [] byte_AES=cipher.doFinal(byte_encode);
            //10.将加密后的数据转换为字符串
            //这里用Base64Encoder中会找不到包
            //解决办法：
            //在项目的Build path中先移除JRE System Library，再添加库JRE System Library，重新编译后就一切正常了。
            String AES_encode=new String(Base64.getEncoder().encode(byte_AES));
            //11.将字符串返回
            return AES_encode;
        } catch (NoSuchAlgorithmException e) {
            System.err.println("NoSuchAlgorithmException:" + e);
        } catch (NoSuchPaddingException e) {
            System.err.println("NoSuchPaddingException:" + e);
        } catch (InvalidKeyException e) {
            System.err.println("InvalidKeyException:" + e);
        } catch (IllegalBlockSizeException e) {
            System.err.println("IllegalBlockSizeException:" + e);
        } catch (BadPaddingException e) {
            System.err.println("BadPaddingException:" + e);
        } catch (UnsupportedEncodingException e) {
            System.err.println("UnsupportedEncodingException:" + e);
        }

        return null;
    }
    /*
     * 解密
     * 解密过程：
     * 1.同加密1-4步
     * 2.将加密后的字符串反纺成byte[]数组
     * 3.将加密内容解密
     */
    public static String AESDecode(String encodeRules, String content){
        try {
            //1.构造密钥生成器，指定为AES算法,不区分大小写
            KeyGenerator keygen= KeyGenerator.getInstance("AES");
            //2.根据ecnodeRules规则初始化密钥生成器
            //生成一个128位的随机源,根据传入的字节数组
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            random.setSeed(encodeRules.getBytes());
            keygen.init(128, random);
            //3.产生原始对称密钥
            SecretKey original_key=keygen.generateKey();
            //4.获得原始对称密钥的字节数组
            byte [] raw=original_key.getEncoded();
            //5.根据字节数组生成AES密钥
            SecretKey key=new SecretKeySpec(raw, "AES");
            //6.根据指定算法AES自成密码器
            Cipher cipher= Cipher.getInstance("AES");
            //7.初始化密码器，第一个参数为加密(Encrypt_mode)或者解密(Decrypt_mode)操作，第二个参数为使用的KEY
            cipher.init(Cipher.DECRYPT_MODE, key);
            //8.将加密并编码后的内容解码成字节数组
            byte [] byte_content= Base64.getDecoder().decode(content);
            /*
             * 解密
             */
            byte [] byte_decode=cipher.doFinal(byte_content);
            String AES_decode=new String(byte_decode,"utf-8");
            return AES_decode;
        } catch (NoSuchAlgorithmException e) {
            System.err.println("NoSuchAlgorithmException:" + e);
        } catch (NoSuchPaddingException e) {
            System.err.println("NoSuchPaddingException:" + e);
        } catch (InvalidKeyException e) {
            System.err.println("InvalidKeyException:" + e);
        } catch (IOException e) {
            System.err.println("IOException:" + e);
        } catch (IllegalBlockSizeException e) {
            System.err.println("IllegalBlockSizeException:" + e);
        } catch (BadPaddingException e) {
            System.err.println("BadPaddingException:" + e);
        }

        return null;
    }

    public static void main(String[] args) {
        AesUtil se=new AesUtil();
        String key = "1ab64b9f2fb408c93d1bbdc48524916";
        String source = "Lb3jM/DYGKCSNvn6Q71Ytws8wwuolmi6s073kV6lu9ikCz/yr2NSI+RvU+2YlSBwiKjwiFncV2WYRqu3hmVKRtM6sY4JwELC3vMY3IaE1pF/xKWOuTXIDInTaEymQYBShQdqV+OitTx9lNd6ljEYl/YNopW/Rzbn0eIKI6Vr1iVZ6f8ND2qXzv4UGKrcOJtT8uXC1YRuV64QRX+ggc47/w==";
//        String secret = se.AESEncode(key, source);
 //       System.out.println("加密后结果:" + secret);
        System.out.println("解密后结果:" + se.AESDecode(key, source));
    }

}