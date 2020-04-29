package com.jh.paymentgateway.util.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Map;

public abstract class AbstractChannel {

	private static final Logger log = LoggerFactory.getLogger(AbstractChannel.class.getSimpleName());
	
	//private final String merchno="sl2018080218221"; // 填写你们自己的商户号
	private final String merchno="shbyt2019071016"; // 填写你们自己的商户号

	//private final String merchkey="043b1eaa"; // 填写你们自己的秘钥
	private final String merchkey="dc3becc5e60764edaa6bb78e4ac05e0e"; // 填写你们自己的秘钥

	private final String version="0100";
	
	public String transcode="";
	
	public String postUrl="http://pay.huanqiuhuiju.com/authsys/api/large/channel/pay/execute.do";
	
	public AbstractChannel(String transcode,String postUrl) {
		
		this.transcode = transcode;
		this.postUrl=postUrl;
	}

	public Map<String,String> allRequestMethod(Map<String,String> map){

		if(map.get("transtype")!=null){
			map.put("transcode","902");  //交易码
		}else {
			map.put("transcode","051");  //交易码
		}
		map.put("version",version);  //版本号
		
		map.put("ordersn",new Date().getTime()+""); //唯一值，交易唯一

		
		map.put("merchno", merchno); //商户号
		
		Map orderbymap =  HashMapConver.getOrderByMap();
		
	    orderbymap.putAll(map);			
	    
	    byte[] response =  HashMapConver.getSign(orderbymap,merchkey);
	    
	    try {
			if(map.get("transtype")!=null){
			postUrl="http://pay.huanqiuhuiju.com/authsys/api/auth/execute.do";
			}
		
	      String result = HttpUtils.post(postUrl, response); //发送post请求

	      System.out.println("返回参数："+result);
	      
	      Map<String ,String> resultMap = JsonUtil.jsonToMap(result);
	      
	      return resultMap;
	      
	    } catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	    return null;
	    
	}
	
}
