package com.jh.paymentgateway.util.xk;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.Map;


/**
 * 	签名验签工具类
 * @author huangqiang
 *
 */
public class SignMessageUtil {
	
	private static Logger logger = LoggerFactory.getLogger(SignMessageUtil.class);
	
	/**
	 * 验签
	 * @param tranData
	 * @param key
	 * @param signType
	 * @return
	 */
	public static boolean verifyMessage(String tranData,String key,String signType) {
		boolean result = false;
		
		if(StringUtils.isBlank(key)) {
			logger.info("验签key配置错误");
			return false;
		}
		try {
			Map<String, String> parms = JsonUtil.jsonStringToMap(tranData);
			//签名串
			String sign = (String) parms.get(PayConfig.SIGNATURE);
			sign = new String(Base64.getDecoder().decode(sign.replaceAll("\\s", "")),"UTF-8");
			logger.info("解码后签名串：{}",sign);
			//去除签名串
			parms.remove(PayConfig.SIGNATURE);
			
			logger.info("验签待加密数据：{}",parms);
			
			//请求参数按字典序排序
			String source = SortAndCovertUtil.covertToStr(parms);
			
			if(PayConfig.SIGNTYPE.equals(signType)) {
				result = MD5Utils.verify(source, sign, key);
			}else {
				logger.info("验签方式配置错误");
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 * 	签名
	 * @param sourceData
	 * @param key
	 * @param signType
	 * @return
	 */
	public static String signMessage(Map<String,String> sourceData,String key,String signType) {
		//待签名数据按字典序排序
		String source = SortAndCovertUtil.covertToStr(sourceData);
		
		if(PayConfig.SIGNTYPE.equals(signType)) {
			if(StringUtils.isBlank(key)) {
				logger.info("签名key配置错误");
				return null;
			}
			try {
				//获取签名串
				String sign = MD5Utils.sign(source, key);
				sourceData.put(PayConfig.SIGNATURE, sign);
				String tranData = JsonUtil.ObjToJsonString(sourceData);
				tranData = Base64.getEncoder().encodeToString(tranData.getBytes("UTF-8"));
				return tranData;
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				logger.info("原数据base64编码失败");
				return null;
			}
			
		}else {
			logger.info("签名方式配置错误");
			return null;
		}
	}
	
	/**
	 * 验签
	 * @param sourceData
	 * @param key
	 * @param signType
	 * @param signature
	 * @return
	 */
	public static boolean verifyMessage(Map<String,String> sourceData,String key,String signType,String signature) {
		boolean result = false;
		
		if(StringUtils.isBlank(key)) {
			logger.info("验签key配置错误");
			return false;
		}
		try {
			//签名串
			String sign = new String(Base64.getDecoder().decode(signature.replaceAll("\\s", "")),"UTF-8");
			logger.info("解码后签名串：{}",sign);
			
			logger.info("验签待加密数据：{}",sourceData);
			
			//请求参数按字典序排序
			String source = SortAndCovertUtil.covertToStr(sourceData);
			
			if(PayConfig.SIGNTYPE.equals(signType)) {
				result = MD5Utils.verify(source, sign, key);
			}else {
				logger.info("验签方式配置错误");
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
}
