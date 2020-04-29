package com.jh.paymentchannel.service;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import com.jh.paymentchannel.util.Util;

import net.sf.json.JSONObject;


@Controller
@EnableAutoConfiguration
public class UNSQuickCallBackService {

	@Value("${ailong.mer_url}")
	private String reqURL;
		
	@Value("${ailong.merId}")
	private String merCode;
	
	@Value("${ailong.key}")
	private String merKey;
	
	private static final Logger log = LoggerFactory.getLogger(UNSQuickCallBackService.class);
	
	@Autowired
	Util util;
	
	private Map<String,String> getParamNames(HttpServletRequest request) {    
        Map<String,String>  map = new HashMap<String,String>();    
        Enumeration<String> paramNames = request.getParameterNames();    
        while (paramNames.hasMoreElements()) {    
            String paramName = (String) paramNames.nextElement();    
    
            String[] paramValues = request.getParameterValues(paramName);    
            if (paramValues.length == 1) {    
                String paramValue = paramValues[0];    
                if (paramValue.length() != 0) {    
                    map.put(paramName, paramValue);    
                }    
            }    
        }    
    
        return map;  
    }  
	
	
	public String getParamsMap(HttpServletRequest request){
		
		
	      Map map = request.getParameterMap();  
	        String text = "";  
	        if (map != null) {  
	            Set set = map.entrySet();  
	            Iterator iterator = set.iterator();  
	            while (iterator.hasNext()) {  
	                Map.Entry entry = (Entry) iterator.next();  
	                if (entry.getValue() instanceof String[]) {  
	                	log.info("==A==entry的key： " + entry.getKey());  
	                    String key = (String) entry.getKey();  
	                    if (key != null && !"id".equals(key) && key.startsWith("[") && key.endsWith("]")) {  
	                        text = (String) entry.getKey();  
	                        break;  
	                    }  
	                    String[] values = (String[]) entry.getValue();  
	                    for (int i = 0; i < values.length; i++) {  
	                    	log.info("==B==entry的value: " + values[i]);  
	                        key += "="+values[i];  
	                    }  
	                    if (key.startsWith("[") && key.endsWith("]")) {  
	                        text = (String) entry.getKey();  
	                        break;  
	                    }  
	                } else if (entry.getValue() instanceof String) {  
	                	log.info("==========entry的key： " + entry.getKey());  
	                	log.info("==========entry的value: " + entry.getValue());  
	                }  
	            }  
	        }  
	        return text;  
		
		
		
	}
	
	/***
	 * 充值回调
	 * 
	 * **/
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/paymentchannel/topup/unspay/notify_call")
	public @ResponseBody void returnCall(HttpServletRequest request, HttpServletResponse response) throws IOException{
	
		
	   log.info("into UNSQuickCallBackService  ================ ");
		
	   request.setCharacterEncoding("utf-8");
	   response.setCharacterEncoding("utf-8");
	   response.setContentType("text/html;charset=utf-8");
	   
	   log.info("进入UNSQuickCallBackService回调接口啦！！！");
	   Map<String,String> resData = new HashMap<String,String>();
	   resData=getParamNames(request);
	   
	   log.info("银生宝支付回调数据/v1.0/paymentchannel/topup/unspay/notify_call:"+resData);
	   
	   PrintWriter out = response.getWriter();
			
		if(resData.get("result_code").equalsIgnoreCase("0000")){
			log.info("result_code  ================ "+resData.get("result_code"));
				String ordercode = resData.get("orderId");
				log.info("ordercode=========="+ordercode);
				
				/**更新订单状态*/
				/**调用下单，需要得到用户的订单信息*/
				RestTemplate restTemplate=new RestTemplate();
				
				URI uri = util.getServiceUrl("transactionclear", "error url request!");
				String url = uri.toString() + "/v1.0/transactionclear/payment/update";
				
				/**根据的用户手机号码查询用户的基本信息*/
				MultiValueMap<String, String> requestEntity  = new LinkedMultiValueMap<String, String>();
				requestEntity.add("status", "1");
				requestEntity.add("order_code",  ordercode);
				
				String result = restTemplate.postForObject(url, requestEntity, String.class);
				
				/**判断是否有外放的通道的处理， 如果有那么继续回调外放哦*/
				uri = util.getServiceUrl("transactionclear", "error url request!");
				url = uri.toString() + "/v1.0/transactionclear/payment/query/ordercode";
				
				
				requestEntity  = new LinkedMultiValueMap<String, String>();
				requestEntity.add("order_code",  ordercode);
				result = restTemplate.postForObject(url, requestEntity, String.class);
				
				JSONObject jsonObject =  JSONObject.fromObject(result);
				JSONObject resultObj  =  jsonObject.getJSONObject("result");
				String outMerOrdercode  =  resultObj.getString("outMerOrdercode");
				String orderdesc        =  resultObj.getString("desc");
				String phone            =  resultObj.getString("phone");
				String  tranamount      =  resultObj.getString("amount");
				String channelTag       =  resultObj.getString("channelTag");
				String notifyURL        =  resultObj.getString("outNotifyUrl");
				if(outMerOrdercode != null && !outMerOrdercode.equalsIgnoreCase("")){
					uri = util.getServiceUrl("channel", "error url request!");
					url = uri.toString() + "/v1.0/channel/callback/yilian/notify_call";
					requestEntity  = new LinkedMultiValueMap<String, String>();
					requestEntity.add("merchant_no",  phone);
					requestEntity.add("amount",  tranamount);
					requestEntity.add("channel_tag",  channelTag);
					requestEntity.add("order_desc",  URLEncoder.encode(orderdesc, "UTF-8"));
					requestEntity.add("order_code",  outMerOrdercode);
					requestEntity.add("sys_order",  ordercode);
					requestEntity.add("notify_url", URLEncoder.encode(notifyURL, "UTF-8"));
					result = restTemplate.postForObject(url, requestEntity, String.class);
				}
		}else {
			String ordercode = resData.get("orderId");
			updateOrder(ordercode, "2");
			
			
		}
		out.write("ok");
		
		out.flush();
		out.close();
	}
	
	
	private String  updateOrder(String ordercode ,String  status) {
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
		return restTemplate.postForObject(url, requestEntity, String.class);
	}
	
	
}
