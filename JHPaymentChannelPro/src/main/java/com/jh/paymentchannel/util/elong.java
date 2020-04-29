package com.jh.paymentchannel.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.jh.paymentchannel.util.ump.paygate.v40.HttpRequest;
public class elong {

	private  static String customerid="10009";
	private  static String nkey="e2aed75213e8da4fbdb0222c24060937";
	private  static String url="http://www.elongpay.com/service/GateWay.aspx";
	private  static String ordercode=System.currentTimeMillis()+"";
	
	public static void main1(String[] args) {
		try {
		Map<String,String> map =new HashMap<String, String>();
		//版本号
		map.put("version", "VERSION_2.0");
		//商户ID
		map.put("customerid",customerid);
		//商户流水号
		map.put("orderNumber", ordercode);
		//订单金额
		map.put("ordermoney", 1+"");
		//通道代码
		map.put("cardNo", "wxjspay" );
		//MD5签名
		map.put("sign", MD5.MD5Encode("customerid={"+customerid+"}&orderNumber={"+ordercode+"}&key={"+nkey+"}").toUpperCase());
		//支付扩展标志
		map.put("postType", "1");
		//返回地址
		map.put("returnurl", "http://ds.jiepaypal.cn/v1.0/paymentchannel/topup/swift/notify_call");		
		//返回地址
		map.put("mobile", "1");
		map.put("goform", "true");
		String Result =  HttpRequest.sendPostMap(url,map);
		
		
		RestTemplate restTemplate=new RestTemplate();
		MultiValueMap<String, String> requestEntity  = new LinkedMultiValueMap<String, String>();
		//版本号
		requestEntity.add("version", "VERSION_2.0");
		//商户ID
		requestEntity.add("customerid",customerid);
		//商户流水号
		requestEntity.add("orderNumber", ordercode);
		//订单金额
		requestEntity.add("ordermoney", 1+"");
		//通道代码
		requestEntity.add("cardNo", "wxjspay" );
		
		String signData="customerid="+customerid+"&orderNumber="+ordercode+"&key="+nkey+"";
		//MD5签名
		requestEntity.add("sign", MD5.MD5Encode(signData));
//		requestEntity.add("sign", MD5("customerid={"+customerid+"}&orderNumber={"+ordercode+"}&key={"+nkey+"}").toUpperCase());
		//支付扩展标志
		requestEntity.add("postType", "1");
		//返回地址
		String notifyurl="http://ds.jiepaypal.cn/v1.0/paymentchannel/topup/swift/notify_call";
		requestEntity.add("returnurl", "http://ds.jiepaypal.cn/v1.0/paymentchannel/topup/swift/notify_call");		
		//返回地址
		requestEntity.add("mobile", "1");
		//返回地址
		requestEntity.add("goform", "true");
		
		String result = restTemplate.postForObject(url, requestEntity, String.class);
		String a=url+"?version=VERSION_2.0&customerid="+ordercode+"&ordermoney="+1+"&cardNo="+"wxjspay"+"&sign="+MD5.MD5Encode(signData).toUpperCase()+"&postType=1&returnurl="+notifyurl+"&mobile=1&goform=true";
		String url = java.net.URLDecoder.decode(urlhtml(result),"utf-8");
		System.out.println(url);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		

	}
	
	private static String urlhtml(String html){
		try {
			
			html=html.substring(html.indexOf("data=")+5, html.indexOf("'/>"));
			
		} catch (Exception e) {
			
			
		}
		
		return html;
	}
	
	 private static String MD5(String sourceStr) {
	        String result = "";
	        try {
	            MessageDigest md = MessageDigest.getInstance("MD5");
	            md.update(sourceStr.getBytes());
	            byte b[] = md.digest();
	            int i;
	            StringBuffer buf = new StringBuffer("");
	            for (int offset = 0; offset < b.length; offset++) {
	                i = b[offset];
	                if (i < 0)
	                    i += 256;
	                if (i < 16)
	                    buf.append("0");
	                buf.append(Integer.toHexString(i));
	            }
	            result = buf.toString();
	            System.out.println("MD5(" + sourceStr + ",32) = " + result);
	            System.out.println("MD5(" + sourceStr + ",16) = " + buf.toString().substring(8, 24));
	        } catch (NoSuchAlgorithmException e) {
	            System.out.println(e);
	        }
	        return result;
	    }

}
