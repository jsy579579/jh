package com.jh.paymentgateway.controller.tldh.util;

import com.jh.paymentgateway.controller.tldh.weixin.WXPayUtil;
import net.sf.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.TreeMap;

public class KKIpayUtil extends WXPayUtil {

	public static String sign(Map<String,String> params,String appkey) throws Exception{
		if(params.containsKey("sign"))//签名明文组装不包含sign字段
			params.remove("sign");
		params.put("key", appkey);
		StringBuilder sb = new StringBuilder();
		for(Map.Entry<String, String> entry:params.entrySet()){
			if(entry.getValue()!=null&&entry.getValue().length()>0){
				sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
			}
		}
		if(sb.length()>0){
			sb.deleteCharAt(sb.length()-1);
		}
		String sign = md5(sb.toString().getBytes("UTF-8"));//记得是md5编码的加签
		System.out.println("sign:"+sign+":"+sb.toString());
		params.remove("key");
		return sign;
	}
	
	 public static boolean validSign(TreeMap<String,String> param,String appkey) throws Exception{
		 if(param!=null&&!param.isEmpty()){
			 if(!param.containsKey("sign"))
	    			return false;
			 String sign = param.get("sign").toString();
			 String mysign = sign(param, appkey);
			 return sign.toLowerCase().equals(mysign.toLowerCase());
		 }
		 return false;
	 }
	 
	 
	 public static Map<String, String> dorequest(String url,Map<String, String> params) throws Exception{
	    String sign= WXPayUtil.generateSignature(params, tlkkIpayConstants.KEY);
//		 params.put("sign", sign(params,tlkkIpayConstants.KEY));
         params.put("sign", sign);
		 HttpConnectionUtil http = new HttpConnectionUtil(url);
		 http.init();
		 byte[] bys = http.postParams(params, true);

		 String result = new String(bys,"UTF-8");
		 Map<String,String> map = handleResult(result);
		 return map;
	 }
	 
	private static Map<String,String> handleResult(String result) throws Exception{
		System.out.println("返回的结果:"+result+"");
		Map map = json2Obj(result, Map.class);
		return map;
	}
	
	public static <T> T json2Obj(String jsonstr,Class<T> cls){
    	JSONObject jo = JSONObject.fromObject(jsonstr);
		T obj = (T) JSONObject.toBean(jo, cls);
		return obj;
    }
	
	public static String md5(byte[] b) {
        try {
        	MessageDigest md = MessageDigest.getInstance("MD5");
        	 md.reset();
             md.update(b);
             byte[] hash = md.digest();
             StringBuffer outStrBuf = new StringBuffer(32);
             for (int i = 0; i < hash.length; i++) {
                 int v = hash[i] & 0xFF;
                 if (v < 16) {
                 	outStrBuf.append('0');
                 }
                 outStrBuf.append(Integer.toString(v, 16).toLowerCase());
             }
             return outStrBuf.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return new String(b);
        }
    }
}
