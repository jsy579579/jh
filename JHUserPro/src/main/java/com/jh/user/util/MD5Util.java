package com.jh.user.util;

import java.security.MessageDigest;

public class MD5Util {

	public static String bytesToHex(byte[] bytes) {
		StringBuffer md5str = new StringBuffer();

		int digital;
		for (int i = 0; i < bytes.length; i++) {
			 digital = bytes[i];
			if(digital < 0) {
				digital += 256;
			}
			if(digital < 16){
				md5str.append("0");
			}
			md5str.append(Integer.toHexString(digital));
		}
		return md5str.toString();
	}
	

	public static String bytesToMD5(byte[] input) {
		String md5str = null;
		try {

			MessageDigest md = MessageDigest.getInstance("MD5");

			byte[] buff = md.digest(input);

			md5str = bytesToHex(buff);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return md5str;
	}

	public static String strToMD5(String str) {
		byte[] input = str.getBytes();
		
		return bytesToMD5(input);
	}

}
