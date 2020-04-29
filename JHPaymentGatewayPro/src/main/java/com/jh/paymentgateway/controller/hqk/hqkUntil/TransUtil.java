package com.jh.paymentgateway.controller.hqk.hqkUntil;

import net.sf.json.JSONObject;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

import java.util.Map;


/*
 * author: 
 *   功能：系统解包和组包方法
 */
public class TransUtil {

	static ObjectMapper mapper = new ObjectMapper();
	static {
		mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.setSerializationInclusion(Inclusion.NON_NULL);
	}

	@SuppressWarnings("unused")
	private String merchkey;
	
	@SuppressWarnings({ "static-access", "unchecked" })
	public String sign(CommonBean trans,String signkey) throws Exception {
		
		if (signkey != null) {
			String resp = this.object2String(trans);
			Map<String, String> resMap = mapper.readValue(resp, Map.class);
			String sign = SignUtil.getSign(resMap, signkey);

			return sign;
		}


		return null;
	}

	@SuppressWarnings("unchecked")
	public byte[] packetCloud(CommonBean trans,String signkey) throws Exception {
		
		if (signkey != null) { 
			String resp = object2String(trans); 
			Map<String, String> resMap = mapper.readValue(resp, Map.class);
			String sign = SignUtil.getCloudSign(resMap, signkey);
			trans.setSign(sign);
		}
		
		byte[] returnData = this.object2Byte(trans);
		
		return returnData;
	}
	
	
	@SuppressWarnings("unchecked") 
	public byte[] packetNewCloud(CommonBean trans, String signkey) throws Exception {

		if (signkey != null) {  
			String resp = object2String(trans); 
			Map<String, String> resMap = mapper.readValue(resp, Map.class);
			String sign = SignUtil.getNewCloudSign(resMap, signkey);
			trans.setSign(sign);
		}
		
		byte[] returnData = this.object2Byte(trans);
		
		return returnData;
	}
	
	@SuppressWarnings({ "unchecked", "static-access" })
	public byte[] packetTele(CommonBean trans,String signkey) throws Exception {
		
		if (signkey != null) {
			String resp = this.object2String(trans);
			Map<String, String> resMap = mapper.readValue(resp, Map.class);
			System.out.println(resMap);
			String sign = SignUtil.getTeleSign(resMap, signkey);  
			trans.setSign(sign);     
		}
		
		byte[] returnData = this.object2Byte(trans);
		
		return returnData;
	}
	
	@SuppressWarnings({ "unchecked", "static-access" })
	public byte[] packetZFB(CommonBean trans,String signkey) throws Exception {
		
		if (signkey != null) {
			String resp = this.object2String(trans);
			Map<String, String> resMap = mapper.readValue(resp, Map.class);
			System.out.println(resMap);
			String sign = SignUtil.getZFBSign(resMap, signkey);  
			trans.setSign(sign);   
		}

		byte[] returnData = this.object2Byte(trans);

		return returnData;
	}
	
	@SuppressWarnings({ "unchecked", "static-access" })
	public byte[] packet(CommonBean trans,String signkey) throws Exception {
		
		if (signkey != null) {
			String resp = this.object2String(trans);
			Map<String, String> resMap = mapper.readValue(resp, Map.class); 
			JSONObject jsonObject = JSONObject.fromObject(resMap);  
			System.out.println(jsonObject.toString());
			String sign = SignUtil.getSign(resMap, signkey);  
			trans.setSign(sign);   
		}
		
		byte[] returnData = this.object2Byte(trans);
		
		return returnData;
	}
	
	@SuppressWarnings({ "unchecked", "static-access" })
	public byte[] packetPublic(CommonBean trans,String signkey) throws Exception {
		
		if (signkey != null) { 
			String resp = this.object2String(trans);
			Map<String, String> resMap = mapper.readValue(resp, Map.class);
			System.out.println(resMap);
			String sign = SignUtil.getPublicSign(resMap, signkey);  
			trans.setSign(sign);   
		}
		
		byte[] returnData = this.object2Byte(trans); 
		
		return returnData;
	}
	
	@SuppressWarnings({ "unchecked", "static-access" })
	public byte[] packetTrans(Trans trans,String signkey) throws Exception {
		
		if (signkey != null) {
			String resp = this.object2String(trans);
			System.out.println(resp);
			Map<String, String> resMap = mapper.readValue(resp, Map.class);
			String sign = SignUtil.getSign(resMap, signkey);
			System.out.println(sign);
			trans.setSign(sign);
		}
		
		byte[] returnData = this.object2Byte(trans);
		
		return returnData;
	}



	public CommonBean json2Trans(byte[] json) {

		CommonBean trans = null;
		try {
			trans = (CommonBean) mapper.readValue(json, CommonBean.class);
		} catch (Exception e) {
		}
		return trans;
	}

	public CommonBean json2Trans(String json) {

		CommonBean trans = null;
		try {
			trans = (CommonBean) mapper.readValue(json, CommonBean.class);
		} catch (Exception e) {
		}
		return trans;
	}


	public String object2json(Object obj) throws Exception {

		if (obj != null) {
			return mapper.writeValueAsString(obj);
		}

		return null;
	}

	public byte[] object2Byte(Object object) {

		byte[] response = null;
		try {
			response = mapper.writeValueAsBytes(object);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return response;
	}

	public static String object2String(Object o) {

		String response = null;
		try {
			if (null != o) {
				response = mapper.writeValueAsString(o);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return response;
	}

}
