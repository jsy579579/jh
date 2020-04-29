 package com.jh.paymentgateway.util.utils;

 import sun.misc.BASE64Encoder;

 import java.io.UnsupportedEncodingException;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;


 public class SecurityUtils
 {
   public static final String Algorithm = "DESede";
 
   public static String EncoderByMd5(String str)
   {
     return EncoderByMd5(str, false);
   }
 
   public static String EncoderByMd5(String str, boolean isLower)
   {
     String encoder = null;
     try
     {
       MessageDigest md5 = MessageDigest.getInstance("MD5");
       if (isLower) {
         encoder = StringUtils.binaryToHexString(md5.digest(str.getBytes("utf-8"))).toLowerCase();
       } else {
         BASE64Encoder base64en = new BASE64Encoder();
         encoder = base64en.encode(md5.digest(str.getBytes("utf-8")));
       }
       md5.digest(str.getBytes("utf-8")).toString().toLowerCase();
     } catch (UnsupportedEncodingException localUnsupportedEncodingException) {
     } catch (NoSuchAlgorithmException localNoSuchAlgorithmException) {
     }
     return encoder;
   }
 

 }
