 package com.jh.paymentgateway.controller.hqm.util;

import java.util.UUID;


 public class StringUtils
 {
   private static String[] binaryArray = { "0000", "0001", "0010", "0011", 
     "0100", "0101", "0110", "0111", 
     "1000", "1001", "1010", "1011", 
     "1100", "1101", "1110", "1111" };
 
   public static String[] chineseDigits = { "零", "一", "二", "三", "四", "五", "六", "七", "八", "九" };
 
   public static String randomUUID() { UUID uuid = UUID.randomUUID();
     return uuid.toString().replace("-", "").toUpperCase(); }
 
   public static String binaryToHexString(byte[] bytes)
   {
     StringBuffer sb = new StringBuffer();
     for (int i = 0; i < bytes.length; i++) {
       String hex = Integer.toHexString(bytes[i] & 0xFF);
       if (hex.length() == 1) {
         hex = '0' + hex;
       }
       sb.append(hex.toUpperCase());
     }
     return sb.toString();
   }
 

 }