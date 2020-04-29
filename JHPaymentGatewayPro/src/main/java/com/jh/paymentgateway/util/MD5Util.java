package com.jh.paymentgateway.util;

/*     */ 
/*     */ import java.io.UnsupportedEncodingException;
/*     */ import java.security.MessageDigest;
/*     */ import java.security.NoSuchAlgorithmException;
/*     */ 
/*     */ public class MD5Util
/*     */ {
/*  13 */   private static final char[] DIGITS_LOWER = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
/*  14 */   private static final char[] DIGITS_UPPER = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
/*     */   private static final String DEFAULT_ENCODING = "UTF8";
/*     */   private static final String ALGORITH = "MD5";
/*  17 */   private static final MessageDigest md = getMessageDigest("MD5");
/*     */ 
/*     */   public static String digest(String srcStr, String encode)
/*     */   {
	 		  byte[] rstBytes;
/*     */     try
/*     */     {
/*  31 */       rstBytes = md.digest(srcStr.getBytes(encode));
/*     */     }
/*     */     catch (UnsupportedEncodingException e)
/*     */     {
/*   
/*  33 */       e.printStackTrace();
/*  34 */       return null;
/*     */     }
/*   6 */     return toHex(rstBytes, true);
/*     */   }
/*     */ 
/*     */   public static String digest(String srcStr)
/*     */   {
/*  47 */     return digest(srcStr, "UTF8");
/*     */   }
/*     */ 
/*     */   private static MessageDigest getMessageDigest(String algorithm)
/*     */   {
/*     */     try
/*     */     {
/*  59 */       return MessageDigest.getInstance(algorithm);
/*     */     } catch (NoSuchAlgorithmException e) {
/*  61 */       e.printStackTrace();
/*  62 */     }return null;
/*     */   }
/*     */ 
/*     */   public static String toHex(byte[] bytes, boolean flag)
/*     */   {
/*  76 */     return new String(processBytes2Hex(bytes, flag ? DIGITS_LOWER : DIGITS_UPPER));
/*     */   }
/*     */ 
/*     */   private static char[] processBytes2Hex(byte[] bytes, char[] digits)
/*     */   {
/*  91 */     int l = bytes.length << 1;
/*  92 */     char[] rstChars = new char[l];
/*  93 */     int j = 0;
/*  94 */     for (int i = 0; i < bytes.length; i++)
/*     */     {
/*  96 */       rstChars[(j++)] = digits[((0xF0 & bytes[i]) >>> 4)];
/*     */ 
/*  98 */       rstChars[(j++)] = digits[(0xF & bytes[i])];
/*     */     }
/* 100 */     return rstChars;
/*     */   }
/*     */   public static String encode(byte[] source) {
/* 103 */     String s = null;
/* 104 */     char[] hexDigits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
/*     */     try {
/* 106 */       MessageDigest md = MessageDigest.getInstance("MD5");
/* 107 */       md.update(source);
/* 108 */       byte[] tmp = md.digest();
/* 109 */       char[] str = new char[32];
/* 110 */       int k = 0;
/* 111 */       for (int i = 0; i < 16; i++) {
/* 112 */         byte byte0 = tmp[i];
/* 113 */         str[(k++)] = hexDigits[(byte0 >>> 4 & 0xF)];
/* 114 */         str[(k++)] = hexDigits[(byte0 & 0xF)];
/*     */       }
/* 116 */       s = new String(str);
/*     */     } catch (Exception e) {
/* 118 */       e.printStackTrace();
/*     */     }
/* 120 */     return s;
/*     */   }
/*     */ }

/* Location:           C:\Users\Admin\Desktop\2.0.1-release\TDBASE-2.0.1-release.jar
 * Qualified Name:     com.tangdi.production.tdbase.util.MD5Util
 * JD-Core Version:    0.6.0
 */