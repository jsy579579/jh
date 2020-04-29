package com.jh.paymentchannel.util.xj;

import java.net.URLEncoder;
import java.security.Key;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;

import org.apache.commons.codec.binary.Base64;

public class SignUtils {
	
	public static String getSign(Map<String,String> params,String key){

		StringBuilder buf = new StringBuilder((params.size() +1) * 10);
		buildPrePayParams(buf,params,false);
		String preStr = buf.toString();

		return  MD5.sign(preStr, "&key=" + key, "utf-8");

	}
	
	public static void buildPrePayParams(StringBuilder sb,Map<String, String> payParams,boolean encoding){
		List<String> keys = new ArrayList<String>(payParams.keySet());
		Collections.sort(keys);
		for(String key : keys){
            String str = payParams.get(key);
            if (str == null || str.length() == 0)
            {
                //空串不参与sign计算
                continue;
            }
			sb.append(key).append("=");
			if(encoding){
				sb.append(urlEncode(str));
			}else{
				sb.append(str);
			}
			sb.append("&");
		}
		sb.setLength(sb.length() - 1);
		System.out.println(sb.toString());
	}
	
	public static String urlEncode(String str){
		try {
			return URLEncoder.encode(str, "UTF-8");
		} catch (Throwable e) {
			return str;
		}
	}
	
	public static String encode(String data, String key) {
		try {
			DESedeKeySpec dks = new DESedeKeySpec(key.getBytes());
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DESede");
			Key secretKey = keyFactory.generateSecret(dks);
			Cipher cipher = Cipher.getInstance("DESede/ECB/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, secretKey, new SecureRandom());
			return Base64.encodeBase64String(cipher.doFinal(data.getBytes("utf-8")));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
