package com.jh.paymentgateway.controller.ld.util;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springside.modules.security.utils.Digests;
import org.springside.modules.utils.Encodes;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.*;

/**
 * 商户可参考本类编写加密和验签的方法，也可直接使用本类
 *
 */

public class SignUtil {
	private static final Log logger = LogFactory.getLog(SignUtil.class);
	public static Map<String, String> paraFilter(Map<String, String> sArray) {
		Map<String, String> result = new HashMap<String, String>();

		if (sArray == null || sArray.size() <= 0) {
			return result;
		}
		DecimalFormat formater = new DecimalFormat("###0.00");
		for (String key : sArray.keySet()) {
			String finalValue = null;
			Object value = sArray.get(key);
			if (value == null || value.equals("")
					|| key.equalsIgnoreCase("sign")) {
				continue;
			}
			if(value instanceof BigDecimal){
				finalValue = formater.format(value);
			}else {
				finalValue = String.valueOf(value);
			}
			
			result.put(key, finalValue);
		}

		return result;
	}

	public static String createLinkString(Map<String, String> params) {
		List<String> keys = new ArrayList<String>(params.keySet());
		Collections.sort(keys);

		String prestr = "";

		for (int i = 0; i < keys.size(); i++) {
			String key = keys.get(i);
			String value = params.get(key);

			if (i == keys.size() - 1) {// 拼接时，不包括最后一个&字符
				prestr = prestr + key + "=" + value;
			} else {
				prestr = prestr + key + "=" + value + "&";
			}
		}
		System.out.println("prestr:" + prestr);
		return prestr;
	}

	public static String genSign(String key, String str){
		return md5(str+"&key="+key).toUpperCase();
	}
	public static String genSign(Map<String, String> map, String key){
		String sign = genSign(key, createLinkString(paraFilter(map)));
		return sign;
	}
    public static String md5(String plainText) {
        try {
        	return Encodes.encodeHex(Digests.md5(new ByteArrayInputStream(plainText.getBytes("utf-8"))));
        } catch (Exception ex) {
            return "";   
        }
    }
    
    public static boolean validSign(Map<String, String> map, String key){
    	String oldSign = map.get("sign");
    	logger.info("oldSign:" + oldSign);
    	logger.info("preSign: " + createLinkString(paraFilter(map)));
    	String sign = genSign(key, createLinkString(paraFilter(map)));
    	return sign.equalsIgnoreCase(oldSign);
    }
    
}
