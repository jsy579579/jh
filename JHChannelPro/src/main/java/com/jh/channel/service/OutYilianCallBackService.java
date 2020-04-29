package com.jh.channel.service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import cn.jh.common.tools.SignMd5Util;
import cn.jh.common.tools.http.HttpClient;

import com.jh.channel.business.OutMerchantSecurityBusiness;
import com.jh.channel.pojo.OutMerchantSecurityKey;
import com.jh.channel.util.Util;

@Controller
@EnableAutoConfiguration
@EnableAsync
public class OutYilianCallBackService {

	private static final Logger log = LoggerFactory.getLogger(OutYilianCallBackService.class);
	
	@Autowired
	Util util;
	
	
	@Autowired
	private OutMerchantSecurityBusiness securityBusiness;
	
    @Async  
    public void callBack(String phone,  String amount,  String channelTag,  
    		String orderdesc, String merordercode,  
    		String sysorder,  String notifyurl,  String sign) {  
       
    	
    	HttpClient httpClient = new HttpClient();
    	
    	StringBuffer  params = new StringBuffer("");
    	
    	params.append("merchant_no="+phone);
    	params.append("&amount="+amount);
    	params.append("&channel_tag="+channelTag);
    	try {
			params.append("&order_desc="+URLEncoder.encode(orderdesc, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	params.append("&order_code="+merordercode);
    	params.append("&sys_order="+sysorder);
    	params.append("&status=000000");
    	params.append("&sign="+sign);
    	String result = httpClient.send(notifyurl, params.toString(), "UTF-8", "UTF-8");
    	
    	/**没有正常调用成功*/
    	if(result == null || !result.equalsIgnoreCase("000000")){
    	
    		
    		RestTemplate restTemplate=new RestTemplate();
    		URI uri = util.getServiceUrl("notice", "error url request!");
    		String url = uri.toString() + "/v1.0/notice/callback/create";
    		MultiValueMap<String, String> requestEntity  = new LinkedMultiValueMap<String, String>();
    		try {
				requestEntity.add("notify_url", URLEncoder.encode(notifyurl, "UTF-8"));
				requestEntity.add("params", URLEncoder.encode(params.toString(), "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		result = restTemplate.postForObject(url, requestEntity, String.class);
    		
    	}
    	
    	
    }  
	
	
	
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/channel/callback/yilian/notify_call")
	public @ResponseBody Object notify_call(HttpServletRequest request, 
			 @RequestParam(value = "merchant_no") String phone,
			 @RequestParam(value = "amount") String amount,
			 @RequestParam(value = "channel_tag") String channeltag,
			 @RequestParam(value = "order_desc") String orderdesc,
			 @RequestParam(value = "order_code") String merordercode,
			 @RequestParam(value = "sys_order") String sysorder,			 
			 @RequestParam(value = "notify_url") String notifyURL
			) throws IOException{
		
		// 设置编码
		orderdesc  =  URLDecoder.decode(orderdesc,  "UTF-8");
		notifyURL  =  URLDecoder.decode(notifyURL,  "UTF-8");
		
		/**根据商户号获取商户的私钥*/
		OutMerchantSecurityKey securityKey = securityBusiness.getOutMerchantSecurityKey(phone);
		
		List<BasicNameValuePair> nvps = new ArrayList<BasicNameValuePair>();
	    nvps.add(new BasicNameValuePair("merchant_no", phone));
	    nvps.add(new BasicNameValuePair("amount", amount));
	    nvps.add(new BasicNameValuePair("channel_tag", channeltag));
	    nvps.add(new BasicNameValuePair("order_desc", orderdesc));
	    nvps.add(new BasicNameValuePair("order_code", merordercode));
	    nvps.add(new BasicNameValuePair("sys_order", sysorder));
	    nvps.add(new BasicNameValuePair("status", "000000"));
	    String createsign = "";
		try {
			createsign  = SignMd5Util.signData(nvps, securityKey.getKey());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		callBack(phone,  amount,  channeltag,  
	    		orderdesc, merordercode,  
	    		sysorder,  notifyURL,  createsign);
		
		return null;
		
		/*String merchantId = request.getParameter("MerchantId");
		String merchOrderId = request.getParameter("MerchOrderId");
		String amount = request.getParameter("Amount");
		String extData = request.getParameter("ExtData");
		String orderId = request.getParameter("OrderId");
		String status = request.getParameter("Status");
		String payTime = request.getParameter("PayTime");
		String settleDate = request.getParameter("SettleDate");
		String sign = request.getParameter("Sign");

		// 需要对必要输入的参数进行检查，本处省略...
		// 订单结果逻辑处理
		String retMsgJson = "";
		try {
			Log.setLogFlag(true);
			Log.println("---交易： 订单结果异步通知-------------------------");
			//验证订单结果通知的签名
			boolean b = false;
			if (!b) {
				retMsgJson = "{\"RetCode\":\"E101\",\"RetMsg\":\"验证签名失败!\"}";
				Log.println("验证签名失败!");
			}else{
				// 签名验证成功后，需要对订单进行后续处理
				
				if ("02".equals(status)) { // 订单已支付;
				//if ("0000".equals(status)) { // 若是互联金融行业, 订单已支付的状态为【0000】
					// 1、检查Amount和商户系统的订单金额是否一致
					// 2、订单支付成功的业务逻辑处理请在本处增加（订单通知可能存在多次通知的情况，需要做多次通知的兼容处理）；
					// 3、返回响应内容
					RestTemplate restTemplate=new RestTemplate();
					
					URI uri = util.getServiceUrl("transactionclear", "error url request!");
					String url = uri.toString() + "/v1.0/transactionclear/payment/update";
					
					*//**根据的用户手机号码查询用户的基本信息*//*
					MultiValueMap<String, String> requestEntity  = new LinkedMultiValueMap<String, String>();
					requestEntity.add("status", "1");
					requestEntity.add("order_code",  merchOrderId);
					String result = restTemplate.postForObject(url, requestEntity, String.class);
					
					retMsgJson = "{\"RetCode\":\"0000\",\"RetMsg\":\"订单已支付\"}";
					Log.println("订单已支付!");
				} else {
					// 1、订单支付失败的业务逻辑处理请在本处增加（订单通知可能存在多次通知的情况，需要做多次通知的兼容处理，避免成功后又修改为失败）；
					// 2、返回响应内容
					retMsgJson = "{\"RetCode\":\"E102\",\"RetMsg\":\"订单支付失败+"+status+"\"}";
					Log.println("订单支付失败!status="+status);
				}
			}
		} catch (Exception e) {
			retMsgJson = "{\"RetCode\":\"E103\",\"RetMsg\":\"处理通知结果异常\"}";
			System.out.println("处理通知结果异常!e="+e.getMessage());
		}
		Log.println("-----处理完成----");
		//返回数据
	    PrintWriter out = response.getWriter();
	    out.println(retMsgJson);
	    out.close(); // for HTTP1.1
	    return null;*/
		
	}

	
}
