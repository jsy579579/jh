package com.jh.paymentgateway.util.hqx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;


public class HashMapConver {
	
	// 排序拼接
	
	private static final Logger log = LoggerFactory.getLogger(HashMapConver.class.getSimpleName());
	
	public static Map getOrderByMap(){
		
		return  new TreeMap<String,String>(new Comparator<String>() {
			
			public int compare(String o1, String o2) {
                return o1.compareTo(o2);      
            }
		});
	}
	
	public static byte[] getSign(Map<String,String> map ,String key){
		
		Set<Entry<String, String>> entrys =   map.entrySet();
		String str = "";
		for(Entry<String, String> entry : entrys ){
			
			str+=entry.getKey()+"="+entry.getValue();

		}
		log.info("签名字符串："+str+key);
	    map.put("sign", SecurityUtils.EncoderByMd5(str+key,true));
	    
	    return 
	    		toJson(map);
	    
	}
	
	public static byte[] toJson(Map<String,String> map ){
		
		String json = JsonUtil.map2Json(map);
		
		System.out.println("请求参数为："+json);
		
	  	return 
	  			json.getBytes();
	}
	

}
