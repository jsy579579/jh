package com.jh.paymentchannel.service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Iterator;

import net.sf.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.jh.paymentchannel.util.Util;
import com.jh.paymentchannel.util.WithDrawOrder;
import com.jh.paymentchannel.util.ump.paygate.v40.HttpRequest;

@Service
public class UNSPayRequest implements PayRequest{
	private static final Logger log = LoggerFactory.getLogger(UNSPayRequest.class);
	
	@Value("${uns.accountId}")
	private String mer_id;
	
	@Value("${uns.payUrl}")
	private String payurl;
	
	@Value("${uns.queryUrl}")
	private String queryurl;
		
	@Value("${uns.key}")
	private String key;
	
	
	
	@Autowired
	Util util;
	
	/*public static void main(String[] args) throws UnsupportedEncodingException {
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("accountId", "2120180316165901001");   //*商户编号

		map.put("name", "钟守韩");	//*用户姓名
		
		map.put("cardNo", "6212261001038982085");  		//*银行卡号
		
		map.put("orderId", "asdasdasdasda");	//*订单号
		
		map.put("purpose", "提现"); 	//*付款目的
		
		map.put("amount", "0.12");  	//*金额
		
		map.put("responseUrl","" );	//*响应地址
		
		map.put("key", "juhe123456");	//*响应地址
		
		String urlParams= getUrlParamsByMap(map);
		
		String md5String = com.jh.paymentchannel.util.uns.Md5Encrypt.md5(urlParams).toUpperCase();
		log.info("usn签名前数据:"+urlParams); 
		log.info("usn签名结果:"+md5String); 
		map.remove("key");
		map.put("mac", md5String);	//*签名结果
		urlParams= getUrlParamsByMap(map);
		log.info("签名后数据:"+urlParams); 
		String Result =  HttpRequest.sendPost("http://pay.unspay.com:8081/delegate-pay-front/delegatePay/pay", urlParams);
		log.info("usn返回结果================"+Result);
		JSONObject jsonObject =  JSONObject.fromObject(Result);
		if(jsonObject.getString("result_code").equals("1000")) {
			log.info("usn返回结果================"+Result);
		}
	  
	}*/
	
	
	@Override
	public WithDrawOrder payRequest(String ordercode,
			String cardno, String username, String amount, String bankname, String phone,  String priOrpub,String notifyURL,String returnURL) {
		WithDrawOrder drawOrder = new WithDrawOrder();
		priOrpub=username+"商户提现";
		StringBuffer sf = new StringBuffer();
		sf.append("accountId=").append(mer_id);
		sf.append("&name=").append(username);
		sf.append("&cardNo=").append(cardno);
		sf.append("&orderId=").append(ordercode);
		sf.append("&purpose=").append(priOrpub);
		sf.append("&amount=").append(amount);
		sf.append("&responseUrl=").append(notifyURL);
		sf.append("&key=").append(key);
		log.info("usn签名前数据:"+sf); 
		String mac;
		try {
			mac = com.jh.paymentchannel.util.uns.Md5Encrypt.md5(sf.toString()).toUpperCase();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			log.error("usn签名报错:"+e.getMessage(),e); 
			updateOrder(ordercode, "2");
			drawOrder.setReqcode("9999");
			drawOrder.setResmsg("提现失败请稍后重试");	
			return drawOrder;
		}
		log.info("usn签名结果:"+mac); 
		 HashMap<String, String> param = new HashMap<String, String>();  
	        param.put("accountId", mer_id); 
	        param.put("name", username); 
	        param.put("cardNo", cardno); 
	        param.put("orderId", ordercode); 
	        param.put("purpose", priOrpub); 
	        param.put("amount", amount); 
	        param.put("responseUrl", notifyURL); 
	        param.put("mac", mac);
		String result;
		try {
			result = com.jh.paymentchannel.util.uns.HttpFormParam.doPost(payurl, param);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			log.error("usn通道请求失败:"+e.getMessage(),e); 
			updateOrder(ordercode, "2");
			drawOrder.setReqcode("99999");
			drawOrder.setResmsg("提现失败请稍后重试");	
			return drawOrder;
		}
//		Map<String, Object> map = new HashMap<String, Object>();
//		map.put("accountId", mer_id);   //*商户编号
//
//		map.put("name", username);	//*用户姓名
//		
//		map.put("cardNo", cardno);  		//*银行卡号
//		
//		map.put("orderId", ordercode);	//*订单号
//		
//		map.put("purpose", priOrpub); 	//*付款目的
//		
//		map.put("amount", amount);  	//*金额
//		
//		map.put("responseUrl", notifyURL);	//*响应地址
//		
//		map.put("key", key);	//*响应地址
//		
//		String urlParams= getUrlParamsByMap(map);
//		
//		String md5String = com.jh.paymentchannel.util.uns.MD5Test.MD5(urlParams);
//		log.info("usn签名前数据:"+urlParams); 
//		log.info("usn签名结果:"+md5String); 
//		map.remove("key");
//		map.put("mac", md5String);	//*签名结果
//		urlParams= getUrlParamsByMap(map);
//		log.info("签名后数据:"+urlParams); 
//		String Result =  HttpRequest.sendPost(payurl, urlParams);
		log.info("usn返回结果================"+result);
		JSONObject jsonObject =  JSONObject.fromObject(result);
		if(jsonObject.getString("result_code").equals("0000")) {
			drawOrder.setReqcode("00000");
			drawOrder.setResmsg(jsonObject.getString("result_msg"));
		}else {
			drawOrder.setReqcode("99999");
			drawOrder.setResmsg("提现失败请稍后重试");
		}
		log.info("AILongPayRequest——drawOrder:"+drawOrder.toString());       
		return drawOrder;
	}

	
	
	private void  updateOrder(String ordercode ,String  status) {
		log.info("ordercode=========="+ordercode);
		
		/**更新订单状态*/
		/**调用下单，需要得到用户的订单信息*/
		RestTemplate restTemplate=new RestTemplate();
		
		URI uri = util.getServiceUrl("transactionclear", "error url request!");
		String url = uri.toString() + "/v1.0/transactionclear/payment/update";
		
		/**根据的用户手机号码查询用户的基本信息*/
		MultiValueMap<String, String> requestEntity  = new LinkedMultiValueMap<String, String>();
		requestEntity.add("status", "2");
		requestEntity.add("order_code",  ordercode);
		restTemplate.postForObject(url, requestEntity, String.class);
	}
	
	@Override
	public WithDrawOrder queryPay(String ordercode) {
		
		WithDrawOrder drawOrder = new WithDrawOrder();
		
		Map<String, Object> map = new HashMap<String, Object>();
		
		map.put("accountId", mer_id);   //*商户编号
		
		map.put("orderId", ordercode);	//*订单号
		
		String urlParams=getUrlParamsByMap(map);
		
		String md5String = com.jh.paymentchannel.util.uns.MD5Test.MD5(urlParams);
		
		log.info("usn签名前数据:"+urlParams); 
		
		log.info("usn签名结果:"+md5String); 
		
		map.put("mac", md5String);	//*签名结果
		
		urlParams=getUrlParamsByMap(map);
		
		log.info("签名后数据:"+urlParams); 
		
		String Result =  HttpRequest.sendPost(payurl, urlParams);
		
		log.info("usn返回结果================"+Result);
		
		JSONObject jsonObject =  JSONObject.fromObject(Result);
		
		if(jsonObject.getString("result_code").equals("0000")) {
			drawOrder.setResmsg(jsonObject.getString("result_msg"));
			
			
		}
		log.info("AILongPayRequest——drawOrder:"+drawOrder.toString());      
		
		return drawOrder;
		
	}
	/** 
	 * 将map转换成url 
	 * @param map 
	 * @return 
	 */  
	public static  String getUrlParamsByMap(Map<String, Object> map) {  
	    if (map == null) {  
	        return "";  
	    }  
	    StringBuffer sb = new StringBuffer();  
	    for (Map.Entry<String, Object> entry : map.entrySet()) {  
	        sb.append(entry.getKey() + "=" + entry.getValue());  
	        sb.append("&");  
	    }  
	    String s = sb.toString();  
	    if (s.endsWith("&")) {  
	        s = org.apache.commons.lang.StringUtils.substringBeforeLast(s, "&");  
	    }  
	    return s;  
	} 
	
	
	/***
	 *商户余额查询
	 * **/
	public Boolean accountBalance(String amount){

		return false;
	}
	
	


}
