package com.jh.paymentgateway.util.np;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;

//import java.io.IOException;
//import org.codehaus.jackson.JsonParseException;
//import org.codehaus.jackson.map.DeserializationConfig;
//import org.codehaus.jackson.map.JsonMappingException;
//import org.codehaus.jackson.map.ObjectMapper;
//import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

/**
 * 签名工具类
 * 
 * @className SignUtil
 * @Description
 * @author xuguiyi
 * @contact
 * @date 2016-6-7 下午11:11:00
 */
public class SignUtil {
	
//	static ObjectMapper mapper = new ObjectMapper();
//	static {
//		mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
//		mapper.setSerializationInclusion(Inclusion.NON_NULL);
//	}

	/**
	 * 根据map key升序排序
	 * @param sortedParams
	 * @return
	 */
	public static String getSign(Map<String, String> sortedParams,String signkey) throws Exception {
		
		StringBuffer signSrc = new StringBuffer();
		List<String> keys = new ArrayList<String>(sortedParams.keySet());
		Collections.sort(keys);
		for (int i = 0; i < keys.size(); i++) {
			String key = keys.get(i);
			String value = String.valueOf(sortedParams.get(key));
			if(value==null||value.trim().length()==0)
				continue;
			if (key != null && !"".equals(key) && value != null && !"sign".equals(key)) {
				signSrc.append(key + "=" + value+"&");
			}
		}
		signSrc.append("key="+signkey);
		System.out.println("拼接待签名字符串=  "+signSrc);
		String sign =new Md5old(signSrc.toString()).get32().toUpperCase();
		
		return sign;
		
	}
	
	/**
	 * 根据map key升序排序
	 * @param sortedParams
	 * @return
	 */
	public static String getSourceData(Map<String, String> sortedParams,String signkey) throws Exception {
		
		StringBuffer signSrc = new StringBuffer();
		List<String> keys = new ArrayList<String>(sortedParams.keySet());
		Collections.sort(keys);
		for (int i = 0; i < keys.size(); i++) {
			String key = keys.get(i);
			String value = sortedParams.get(key);
			if (key != null && !"".equals(key) && value != null && !"sign".equals(key)) {
				signSrc.append(key + "=" + value);
			}
		}
		
		return signSrc.toString();
		
	}
	
	/**
	 * 根据map key升序排序
	 * @param sortedParams
	 * @return
	 */
	public String localSign(String sourceData,String signkey) throws Exception {
		
		String sign = new Md5old(sourceData).get16();
		
		return sign;
	}
	
	/*public static void main(String[] args) throws Exception {
		//{"transcode":"001","merchno":"001","dsorderid":"2016060700","regno":"222222","compayname":"阿里巴巴","frname":"马云","version":"0100","ordersn":"201511130000003"}
	
//		String json = "{\"transcode\":\"001\",\"merchno\":\"000000000000000\",\"dsorderid\":\"2016060700\",\"regno\":\"222222\",\"compayname\":\"阿里巴巴\",\"frname\":\"马云\",\"version\":\"0100\",\"ordersn\":\"201511130000003\"}";
//	
//		Map<String,String> map = mapper.readValue(json, Map.class);
//		
//		System.out.println(getSign(map,"c26c44"));
	}*/
	
	
	

}
