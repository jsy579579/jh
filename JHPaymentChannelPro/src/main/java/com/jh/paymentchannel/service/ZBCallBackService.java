package com.jh.paymentchannel.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sf.json.JSONObject;
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

import com.alibaba.fastjson.JSON;
import com.jh.paymentchannel.util.Util;
import com.jh.paymentchannel.util.zb.MD5Util;

@Controller
@EnableAutoConfiguration
public class ZBCallBackService {

	private static final Logger log = LoggerFactory.getLogger(ZBCallBackService.class);
	
	
	
	@Autowired
	Util util;
	@Value("${payment.ipAddress}")
	private String ipAddress;
	@Value("${zb.appid}")
	private String appId ;
	@Value("${zb.appkey}")
	private String appKey ;
	@Value("${zb.pay_url}")
	private String payUrl ;
	/**
	 * 获取请求参数中所有的信息
	 * 
	 * @param request
	 * @return
	 */
	public static Map<String, String> getAllRequestParam( HttpServletRequest request) {
		Map<String, String> res = new HashMap<String, String>();
		Enumeration<?> temp = request.getParameterNames();
		if (null != temp) {
			while (temp.hasMoreElements()) {
				String en = (String) temp.nextElement();
				String value = request.getParameter(en);
				res.put(en, value);
				if (null == res.get(en) || "".equals(res.get(en))) {
					res.remove(en);
				}
			}
		}
		return res;
	}
	
	public static final byte[] readBytes(InputStream is, int contentLen) {
        if (contentLen > 0) {
                int readLen = 0;
                int readLengthThisTime = 0;
                byte[] message = new byte[contentLen];
                try {
                    while (readLen != contentLen) {
                        readLengthThisTime = is.read(message, readLen, contentLen- readLen);
                        if (readLengthThisTime == -1) {// Should not happen.
                           break;
                        }
                        readLen += readLengthThisTime;
                    }
                    return message;
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }

        return new byte[] {};
}
	
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/paymentchannel/topup/zb/notify_call")
	public @ResponseBody void notifycall(HttpServletRequest request, HttpServletResponse response) throws IOException{
	       String resString = "fail";
	       log.info("收到上游异步通知=======");
	       request.setCharacterEncoding("UTF-8");
		    int size = request.getContentLength();
		    log.info(size+"");
		    InputStream is = request.getInputStream();
		    byte[] reqBodyBytes = readBytes(is, size);
		    
		    String json = new String(reqBodyBytes);
		    log.info("ͨ通知结果参数："+json);
		    
		    HashMap<String,String> reqParam = (HashMap)JSON.parseObject(json, HashMap.class);
		    log.info("获取到异步通知数据：" + JSON.toJSONString(reqParam));
            try
            {
    		    log.info("获取到异步通知数据：" + JSON.toJSONString(reqParam));
    			//校验数据库，如果数据库已经记录该笔订单支付成功，则直接返回success--商户自己处理业务逻辑
    			//校验签名
    			String sign=MD5Util.doEncrypt(reqParam, appKey);
                //充值回调方法
                if(sign.equalsIgnoreCase(reqParam.get("signature"))&&reqParam.get("paySt").equals("2")){
	                //商户可加入自己的处理逻辑
	                /**更新订单状态*/
					/**调用下单，需要得到用户的订单信息*/
					RestTemplate restTemplate=new RestTemplate();
					
					URI uri = util.getServiceUrl("transactionclear", "error url request!");
					String url = uri.toString() + "/v1.0/transactionclear/payment/update";
					
					/**根据的用户手机号码查询用户的基本信息*/
					MultiValueMap<String, String> requestEntity  = new LinkedMultiValueMap<String, String>();
					requestEntity.add("status", "1");
					requestEntity.add("order_code",   reqParam.get("mchntOrderNo"));
					requestEntity.add("third_code",   reqParam.get("orderNo"));
					String result = restTemplate.postForObject(url, requestEntity, String.class);
					
					/**判断是否有外放的通道的处理， 如果有那么继续回调外放哦*/
					uri = util.getServiceUrl("transactionclear", "error url request!");
					url = uri.toString() + "/v1.0/transactionclear/payment/query/ordercode";
					
					
					requestEntity  = new LinkedMultiValueMap<String, String>();
					requestEntity.add("order_code",   reqParam.get("mchntOrderNo"));
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
						requestEntity.add("sys_order",    reqParam.get("mchntOrderNo"));
						requestEntity.add("notify_url", URLEncoder.encode(notifyURL, "UTF-8"));
						result = restTemplate.postForObject(url, requestEntity, String.class);
					}
					resString="success";
                	//提现回调方法
                }else if(sign.equalsIgnoreCase(reqParam.get("signature"))&&(reqParam.get("paySt").equals("4")||reqParam.get("paySt").equals("3"))){
	                //商户可加入自己的处理逻辑
	                /**更新订单状态*/
					/**调用下单，需要得到用户的订单信息*/
					RestTemplate restTemplate=new RestTemplate();
					
					URI uri = util.getServiceUrl("transactionclear", "error url request!");
					String url = uri.toString() + "/v1.0/transactionclear/payment/update";
					
					/**根据的用户手机号码查询用户的基本信息*/
					MultiValueMap<String, String> requestEntity  = new LinkedMultiValueMap<String, String>();
					requestEntity.add("status", "1");
					requestEntity.add("order_code",   reqParam.get("mchntOrderNo"));
					requestEntity.add("third_code",   reqParam.get("orderNo"));
					String result = restTemplate.postForObject(url, requestEntity, String.class);
					
					/**判断是否有外放的通道的处理， 如果有那么继续回调外放哦*/
					uri = util.getServiceUrl("transactionclear", "error url request!");
					url = uri.toString() + "/v1.0/transactionclear/payment/query/ordercode";
					
					
					requestEntity  = new LinkedMultiValueMap<String, String>();
					requestEntity.add("order_code",  reqParam.get("mchntOrderNo"));
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
						requestEntity.add("sys_order",   reqParam.get("mchntOrderNo"));
						requestEntity.add("notify_url", URLEncoder.encode(notifyURL, "UTF-8"));
						result = restTemplate.postForObject(url, requestEntity, String.class);
					}
					resString="success";
                }
                

            } catch (Exception e)
            {
                //如果验签失败，则抛出异常，返回ret_code=1111
            	resString="fail";
            }
	        
	       PrintWriter write = response.getWriter();
	       write.write(resString);
	       write.flush();
	       write.close();
	}
		
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/paymentchannel/topup/zb/ret_url")
	public  String retcall(HttpServletRequest req, HttpServletResponse response){
		HashMap resData = new HashMap();
		HashMap data = new HashMap();
		String resString = "";
		try
		{
			Map<String, String> reqParam =getAllRequestParam(req);
			log.info("获取到同步通知数据：" + JSON.toJSONString(reqParam));
			//校验签名
			String sign=MD5Util.doEncrypt(reqParam, appKey);
			if(sign.equalsIgnoreCase(reqParam.get("signature"))) {
				if(reqParam.get("paySt").equals("0")||reqParam.get("paySt").equals("3")||reqParam.get("paySt").equals("4")) {
					return "redirect:http://1.xinli2017.applinzi.com/login/zhufusb.html";
					
				}else {
					return  "redirect:http://1.xinli2017.applinzi.com/login/zhufucg.html";
				}
				
			}else {
				return  "redirect:http://1.xinli2017.applinzi.com/login/zhufusb.html";
			}
		
		    
		} catch (Exception e)
		{
			 //如果验签失败，则抛出异常，返回ret_code=1111
			log.debug("验证签名发生异常" + e);
			return "redirect:http://1.xinli2017.applinzi.com/login/zhufusb.html";
		}
	}

	@RequestMapping(method=RequestMethod.GET,value="/v1.0/paymentchannel/topup/zb/ret_url")
	public  String retGetcall(HttpServletRequest req, HttpServletResponse response){
		HashMap resData = new HashMap();
		HashMap data = new HashMap();
		String resString = "";
		try
		{
			Map<String, String> reqParam =getAllRequestParam(req);
			log.info("获取到同步通知数据：" + JSON.toJSONString(reqParam));
			//校验签名
			String sign=MD5Util.doEncrypt(reqParam, appKey);
			if(sign.equalsIgnoreCase(reqParam.get("signature"))) {
				if(reqParam.get("paySt").equals("0")||reqParam.get("paySt").equals("3")||reqParam.get("paySt").equals("4")) {
					return "redirect:http://1.xinli2017.applinzi.com/login/zhufusb.html";
					
				}else {
					return  "redirect:http://1.xinli2017.applinzi.com/login/zhufucg.html";
				}
				
			}else {
				return  "redirect:http://1.xinli2017.applinzi.com/login/zhufusb.html";
			}
		
		    
		} catch (Exception e)
		{
			 //如果验签失败，则抛出异常，返回ret_code=1111
			log.debug("验证签名发生异常" + e);
			return "redirect:http://1.xinli2017.applinzi.com/login/zhufusb.html";
		}
	}
	
}
