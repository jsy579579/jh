package com.jh.paymentgateway.util.tl;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;

/**
 * 签名工具
 * @author hy
 * @date 2018年3月29日
 */
public class Signature {
	private static Log log = LogFactory.getLog(Signature.class);
	
	private static final String RSA_PATH = "d:/";
	private static final String RSA_POST = ".cer";
	private static final String IP = "IP";
	
	/**
	 * 获取MD5签名, key小写
	 * @param jsonObject
	 * @param key
	 * @return
	 */
	public static String getSignMD5sk(JSONObject jsonObject, String key) {
		String result = getBase(jsonObject);
		result += "&key=" + key;
		log.info("Sign Before MD5:" + result);
		result = MD5.getMD5Str(result).toUpperCase();
		log.info("Sign Result:" + result);
		return result;
	}

	/**
	 * 获取MD5签名
	 * @param jsonObject
	 * @param key
	 * @return
	 */
	public static String getSignMD5(JSONObject jsonObject, String key) {
		String result = getBase(jsonObject);
		result += "&KEY=" + key;
		log.info("Sign Before MD5:" + result);
		result = MD5.getMD5Str(result).toUpperCase();
		log.info("Sign Result:" + result);
		return result;
	}
	
	/**
	 * 获取待签名串
	 * @param jsonObject
	 * @return
	 */
	public static String getBase(JSONObject jsonObject) {
		ArrayList<String> list = new ArrayList<String>();
		for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
			// 空值，ip不参与签名
			if (entry.getValue() != null && !"".equals(entry.getValue().toString()) 
					&& !entry.getKey().equalsIgnoreCase(IP)) {
				if(entry.getValue() instanceof JSONObject){
					// 内部为json时，也要排序， 用SerializerFeature.SortField 不生效，太复杂了
					Map<String, Object> map = new TreeMap<String,Object>();
					for (Map.Entry<String, Object> en : ((JSONObject)entry.getValue()).entrySet()) {
						map.put(en.getKey(), en.getValue());
					}
					StringBuffer sb = new StringBuffer();
					sb.append("{");
					Iterator<String> it = map.keySet().iterator();  
			        while (it.hasNext()) {
			        	String kkey = it.next();
			            sb.append("\"").append(kkey).append("\"").append(":").append("\"").append(map.get(kkey)).append("\",");
			        }
			        String re = sb.toString();
			        list.add(entry.getKey() + "=" + re.substring(0, re.length()-1) + "}&");
				}else{
					list.add(entry.getKey() + "=" + entry.getValue() + "&");
				}
			}
		}
		int size = list.size();
		String[] arrayToSort = list.toArray(new String[size]);
		Arrays.sort(arrayToSort, String.CASE_INSENSITIVE_ORDER);
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < size; i++) {
			sb.append(arrayToSort[i]);
		}
		String result = sb.toString();
		return result.substring(0, result.length()-1);
	}

	/**
	 * MD5验签
	 * @param json
	 * @param key
	 * @return
	 */
	public static boolean checkSignMD5(JSONObject json, String key) {
		String signRequest = json.getString("SIGN");
		if (StringUtils.isEmpty(signRequest)) {
			log.info("请求的数据签名数据不存在，有可能被第三方篡改!!!");

			return false;
		}
		log.info("请求的签名是:" + signRequest);
		//清掉返回数据对象里面的Sign数据（不能把这个数据也加进去进行签名），然后用签名算法进行签名
		json.put("SIGN", "");
		//将请求的数据根据用签名算法进行计算新的签名，用来跟请求的签名进行比较
		String sign = Signature.getSignMD5(json, key);

		if (!StringUtils.equals(sign, signRequest)) {
			log.info("请求的数据签名验证不通过，有可能被第三方篡改!!!");

			return false;
		}
		log.info("恭喜，请求的数据签名验证通过!!!");

		return true;
	}

	/**
	 * SHA1WithRSA验签
	 * @param json
	 * @param channelId
	 * @return
	 */
	public static boolean checkSignRSA(JSONObject json, String path) {
		String signRequest = json.getString("SIGN");
		if (StringUtils.isEmpty(signRequest)) {
			log.info("请求的数据签名数据不存在，有可能被第三方篡改!!!");

			return false;
		}
		log.info("请求的签名是:" + signRequest);
		//清掉返回数据对象里面的Sign数据（不能把这个数据也加进去进行签名），然后用签名算法进行签名
		json.put("SIGN", "");

		if (!CertUtil.verifySign(getBase(json), signRequest, path)) {
			log.info("请求的数据签名验证不通过，有可能被第三方篡改!!!");

			return false;
		}
		log.info("恭喜，请求的数据签名验证通过!!!");

		return true;
	}
	


}
