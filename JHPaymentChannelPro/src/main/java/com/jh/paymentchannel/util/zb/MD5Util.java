package com.jh.paymentchannel.util.zb;

import java.util.Arrays;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;

public class MD5Util {
	public static String doEncrypt(Map<String, String> map,String merKey) {
		try{
			Object[] keys =  map.keySet().toArray();
			Arrays.sort(keys);
			StringBuilder originStr = new StringBuilder();
			for(Object key:keys){
				if(null!=map.get(key)&&!"".equals(map.get(key).toString())&&!key.equals("signature"))
				originStr.append(key).append("=").append(map.get(key)).append("&");
			}
			originStr.append("key=").append(merKey);
			System.out.println("md5签名前拼接串："+originStr.toString());
			String sign = DigestUtils.md5Hex(originStr.toString().getBytes("utf-8"));
			System.out.println("md5签名值sign="+sign);
			return sign;
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}
}
