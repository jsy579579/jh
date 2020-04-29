package com.jh.paymentgateway.controller.hqk.until;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Map;

public abstract class AbstractChannel {

	private static final Logger log = LoggerFactory.getLogger(AbstractChannel.class.getSimpleName());
	
	private final String merchno="shbyt2019071016"; // 填写你们自己的商户号
	
	private final String merchkey="dc3becc5e60764edaa6bb78e4ac05e0e"; // 填写你们自己的秘钥

	private final String version="0100";
	
	public String transcode="";
	
	public String postUrl="http://pay.huanqiuhuiju.com/authsys/api/sdj/pay/execute.do";
	
	public AbstractChannel(String transcode, String postUrl) {
		this.transcode = transcode;
		this.postUrl=postUrl;
	}

	public Map<String,String> allRequestMethod(Map<String,String> map){

        if(map.get("transtype")!=null){
            map.put("transcode","902");  //交易码
        }else {
            map.put("transcode","053");  //交易码
        }

		map.put("version",version);
		
		map.put("ordersn",new Date().getTime()+"");
		
		map.put("merchno", merchno);
		
		Map orderbymap =  HashMapConver.getOrderByMap();
		
	    orderbymap.putAll(map);
        System.out.println(orderbymap.toString());
	    byte[] response =  HashMapConver.getSign(orderbymap,merchkey);
	    
	    try {
		
	      String result = HttpUtils.post(postUrl, response);

	      System.out.println("返回参数："+result);
	      
	      Map<String ,String> resultMap = JsonUtil.jsonToMap(result);
	      
	      return resultMap;
	      
	    } catch (Exception e) {

			e.printStackTrace();
		}
		
	    return null;
	    
	}
	
}
