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
import com.jh.paymentchannel.util.ump.exception.VerifyException;
import com.jh.paymentchannel.util.ump.paygate.v40.Mer2Plat_v40;
import com.jh.paymentchannel.util.ump.paygate.v40.Plat2Mer_v40;

import net.sf.json.JSONObject;


@Controller
@EnableAutoConfiguration
public class UMPCallBackService {

	private static final Logger log = LoggerFactory.getLogger(UMPCallBackService.class);
	
	@Value("${jifu.hzfPriKey}")
	private  String hzfPriKey1;
	
	@Autowired
	Util util;
	private Map getParamNames(HttpServletRequest request) {    
        Map map = new HashMap();    
        Enumeration paramNames = request.getParameterNames();    
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
	
	
	@RequestMapping(method=RequestMethod.GET,value="/v1.0/paymentchannel/topup/ump/notify_call")
	public @ResponseBody void notifycall(HttpServletRequest request, HttpServletResponse response) throws IOException{
		   HashMap resData = new HashMap();
	       HashMap data = new HashMap();
	       String resString = "";
	       String mer_id = request.getParameter("mer_id");
	       if(!"".equals(mer_id) && null != mer_id)
	        {
	            try
	            {
	            	 log.debug("联动验签前");
	                //验签,不抛异常表示验签成功
	                data =(HashMap) Plat2Mer_v40.getPlatNotifyData(request);
	                log.info("联动验签成功=============== "+data==null?null:data.toString());
	                //验签成功 ，
	                resData.put("mer_id", data.get("mer_id")); //数据可以从data里边取，也可以从request里边取。
	                resData.put("sign_type", data.get("sign_type"));
	                resData.put("version", data.get("version"));
	                resData.put("mer_date", data.get("mer_date"));
	                resData.put("ret_code", "0000");
	                //充值回调方法
	                if( data.get("error_code")!=null&&data.get("error_code").equals("0000")&&data.get("trade_state").equals("TRADE_SUCCESS")){
	                	resString = Mer2Plat_v40.merNotifyResData(resData);
		                //商户可加入自己的处理逻辑
		                /**更新订单状态*/
						/**调用下单，需要得到用户的订单信息*/
						RestTemplate restTemplate=new RestTemplate();
						
						URI uri = util.getServiceUrl("transactionclear", "error url request!");
						String url = uri.toString() + "/v1.0/transactionclear/payment/update";
						
						/**根据的用户手机号码查询用户的基本信息*/
						MultiValueMap<String, String> requestEntity  = new LinkedMultiValueMap<String, String>();
						requestEntity.add("status", "1");
						requestEntity.add("order_code",   data.get("order_id").toString());
						requestEntity.add("third_code",   data.get("trade_no").toString());
						String result = restTemplate.postForObject(url, requestEntity, String.class);
						
						/**判断是否有外放的通道的处理， 如果有那么继续回调外放哦*/
						uri = util.getServiceUrl("transactionclear", "error url request!");
						url = uri.toString() + "/v1.0/transactionclear/payment/query/ordercode";
						
						
						requestEntity  = new LinkedMultiValueMap<String, String>();
						requestEntity.add("order_code",   data.get("order_id").toString());
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
							requestEntity.add("sys_order",   data.get("order_id").toString());
							requestEntity.add("notify_url", URLEncoder.encode(notifyURL, "UTF-8"));
							result = restTemplate.postForObject(url, requestEntity, String.class);
						}
	                	//提现回调方法
	                }else if(data.get("ret_code")!=null&&data.get("ret_code").equals("0000")&&data.get("trade_state").equals("4")){
	                	resString = Mer2Plat_v40.merNotifyResData(resData);
	                //商户可加入自己的处理逻辑
	                /**更新订单状态*/
					/**调用下单，需要得到用户的订单信息*/
					RestTemplate restTemplate=new RestTemplate();
					
					URI uri = util.getServiceUrl("transactionclear", "error url request!");
					String url = uri.toString() + "/v1.0/transactionclear/payment/update";
					
					/**根据的用户手机号码查询用户的基本信息*/
					MultiValueMap<String, String> requestEntity  = new LinkedMultiValueMap<String, String>();
					requestEntity.add("status", "1");
					requestEntity.add("order_code",   data.get("order_id").toString());
					requestEntity.add("third_code",   data.get("trade_no").toString());
					String result = restTemplate.postForObject(url, requestEntity, String.class);
					
					/**判断是否有外放的通道的处理， 如果有那么继续回调外放哦*/
					uri = util.getServiceUrl("transactionclear", "error url request!");
					url = uri.toString() + "/v1.0/transactionclear/payment/query/ordercode";
					
					
					requestEntity  = new LinkedMultiValueMap<String, String>();
					requestEntity.add("order_code",   data.get("order_id").toString());
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
						requestEntity.add("sys_order",   data.get("order_id").toString());
						requestEntity.add("notify_url", URLEncoder.encode(notifyURL, "UTF-8"));
						result = restTemplate.postForObject(url, requestEntity, String.class);
					}
	                	
	                	
	                }
	                

	            } catch (VerifyException e)
	            {
	                //如果验签失败，则抛出异常，返回ret_code=1111
	                System.out.println("验证签名发生异常" + e);
	                resData.put("mer_id", data.get("mer_id")); //数据可以从data里边取，也可以从request里边取。
	                resData.put("sign_type", data.get("sign_type"));
	                resData.put("version", data.get("version"));
	                resData.put("mer_date", data.get("mer_date"));
	                resData.put("ret_code", "9999");
	                resString = Mer2Plat_v40.merNotifyResData(resData);
	            }
	        }
	       PrintWriter write = response.getWriter();
	       resString = "<html><META NAME=\"MobilePayPlatform\" CONTENT=\"" + resString + "\" /></html>" ;
	       write.print(resString);
	       write.flush();
	       
	}
		
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/paymentchannel/topup/ump/ret_url")
	public  String retcall(HttpServletRequest request, HttpServletResponse response) throws IOException{
		
		 HashMap resData = new HashMap();
	       HashMap data = new HashMap();
	       String resString = "";
	       String mer_id = request.getParameter("mer_id");
	       if(!"".equals(mer_id) && null != mer_id)
	        {
	            try
	            {
	                //验签,不抛异常表示验签成功
	            	log.debug("联动验签前");
	                data = (HashMap)Plat2Mer_v40.getPlatNotifyData(request);
	                log.debug("联动验签成功 "+data==null?null:data.toString());
	                //验签成功 ，
	                resData.put("ret_code","0000");
	                resData.put("mer_id", data.get("mer_id")); //数据可以从data里边取，也可以从request里边取。
	                resData.put("sign_type", data.get("sign_type"));
	                resData.put("version", data.get("version"));
	                resData.put("order_id", data.get("order_id"));
	                resData.put("mer_date", data.get("mer_date"));

	                resString = Mer2Plat_v40.merNotifyResData(resData);

	              return  "redirect:http://1.xinli2017.applinzi.com/login/zhufucg.html";
	            } catch (VerifyException e)
	            {
	            	 //如果验签失败，则抛出异常，返回ret_code=1111
	                log.debug("验证签名发生异常" + e);
	                resData.put("ret_code","1111");
	                resString = Mer2Plat_v40.merNotifyResData(resData);
	            	return "redirect:http://1.xinli2017.applinzi.com/login/zhufusb.html";
	            }
	        }
		
		
		return "redirect:http://1.xinli2017.applinzi.com/login/zhufusb.html";
	}


	
}
