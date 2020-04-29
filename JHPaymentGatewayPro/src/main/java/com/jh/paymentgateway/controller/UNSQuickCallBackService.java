package com.jh.paymentgateway.controller;

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

import com.jh.paymentgateway.basechannel.BaseChannel;
import com.jh.paymentgateway.common.ChannelUtils;
import com.jh.paymentgateway.config.RedisUtil;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;
import com.jh.paymentgateway.util.Util;

import net.sf.json.JSONObject;


@Controller
@EnableAutoConfiguration
public class UNSQuickCallBackService extends BaseChannel {

	
	private static final Logger log = LoggerFactory.getLogger(UNSQuickCallBackService.class);
	@Autowired
	private RedisUtil redisUtil;

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
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/paymentgateway/topup/unspay/notify_call")
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
	   String ordercode = resData.get("orderId");
		log.info("ordercode=========="+ordercode);
		PaymentRequestParameter bean = redisUtil.getPaymentRequestParameter(ordercode);		
		if(resData.get("result_code").equalsIgnoreCase("0000")){
			log.info("result_code  ================ "+resData.get("result_code"));
			this.updateSuccessPaymentOrder(bean.getIpAddress(), ordercode,"");	
		}else {
			this.updateStatusPaymentOrder(bean.getIpAddress(), "2", ordercode, "");	
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
		
		String url = uri.toString()+ChannelUtils.getCallBackUrl(uri.toString());
		//String url = uri.toString() + "/v1.0/transactionclear/payment/update";
		
		/**根据的用户手机号码查询用户的基本信息*/
		MultiValueMap<String, String> requestEntity  = new LinkedMultiValueMap<String, String>();
		requestEntity.add("status", "2");
		requestEntity.add("order_code",  ordercode);
		return restTemplate.postForObject(url, requestEntity, String.class);
	}
	
	
}
