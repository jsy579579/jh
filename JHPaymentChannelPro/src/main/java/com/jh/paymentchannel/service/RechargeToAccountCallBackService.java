package com.jh.paymentchannel.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.jh.paymentchannel.basechannel.BaseChannel;
import com.jh.paymentchannel.util.AlipayServiceEnvConstants;
import com.jh.paymentchannel.util.Util;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;
import net.sf.json.JSONObject;


@Controller
@EnableAutoConfiguration
public class RechargeToAccountCallBackService extends BaseChannel{

	private static final Logger LOG = LoggerFactory.getLogger(RechargeToAccountCallBackService.class);
	
	@Autowired
	Util util;
	
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/paymentchannel/topup/rechargetoaccount/notify_call")
	public @ResponseBody Object notifycall(HttpServletRequest request, HttpServletResponse response) throws IOException{
		  
		Map<String,String> params = new HashMap<String,String>();
 		Map requestParams = request.getParameterMap();
 		
 		for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext();) {
 		    String name = (String) iter.next();
 		    String[] values = (String[]) requestParams.get(name);
 		    String valueStr = "";
 		    for (int i = 0; i < values.length; i++) {
 		        valueStr = (i == values.length - 1) ? valueStr + values[i]
 		                    : valueStr + values[i] + ",";
 		  	}

 			params.put(name, valueStr);
 		}
 		LOG.info("支付宝WAP回调进来了============params:"+params);
 		
 		String realChannelOrderCode = params.get("trade_no");
 		String orderCode = params.get("out_trade_no");
 		String amount = params.get("total_amount");
 		String tradeStatus = params.get("trade_status");
 		
 		Map<String, Object> queryOrdercode = this.queryOrdercode(orderCode);
 		Object object = queryOrdercode.get("result");
		JSONObject fromObject = JSONObject.fromObject(object);
		JSONObject resultObj = fromObject.getJSONObject("result");
 		
		AlipayServiceEnvConstants alipayClient = new AlipayServiceEnvConstants();
		boolean flag = false;
 		if(alipayClient != null) {
		 		try {
					flag = AlipaySignature.rsaCheckV1(params,AlipayServiceEnvConstants.ALIPAY_PUBLIC_KEY, AlipayServiceEnvConstants.CHARSET,"RSA2");
				} catch (AlipayApiException e) {
					e.printStackTrace();
		 			return ResultWrap.err(LOG, CommonConstants.FALIED, "验签异常");
				}
 		}else{
 			return ResultWrap.err(LOG, CommonConstants.FALIED, "验签失败,无支付宝密钥配置");
 		}
 		
 		if(!flag) {
 			return ResultWrap.err(LOG, CommonConstants.FALIED, "验签失败");
 		}
 		
 		
 		
		
		if(!"TRADE_SUCCESS".equalsIgnoreCase(tradeStatus) && !"TRADE_FINISHED".equalsIgnoreCase(tradeStatus)) {
			return ResultWrap.err(LOG, CommonConstants.FALIED, "非成功回调");
		}
		
		
		if(new BigDecimal(resultObj.getString("amount")).compareTo(new BigDecimal(amount)) != 0) {
			return ResultWrap.err(LOG, CommonConstants.FALIED, "验证金额失败");
		}
		if("1".equals(resultObj.getString("status"))) {
			return ResultWrap.err(LOG, CommonConstants.FALIED, "订单已处理");
		}
		
		RestTemplate restTemplate = new RestTemplate();

		URI uri = util.getServiceUrl("transactionclear", "error url request!");
		String url = uri.toString() + "/v1.0/transactionclear/payment/update";

		// **根据的用户手机号码查询用户的基本信息*//*
		MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("status", "1");
		requestEntity.add("third_code", "");
		requestEntity.add("order_code", orderCode);
		String result = restTemplate.postForObject(url, requestEntity, String.class);

		Map<String, Object> queryOrdercode1 = this.queryOrdercode(orderCode);
		Object object1 = queryOrdercode1.get("result");
		fromObject = JSONObject.fromObject(object1);
		resultObj = fromObject.getJSONObject("result");

		String realAmount = resultObj.getString("realAmount");
		String userId = resultObj.getString("userid");
		
		uri = util.getServiceUrl("user", "error url request!");
		url = uri.toString() + "/v1.0/user/account/update";
		requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("user_id", userId);
		requestEntity.add("amount", realAmount);
		requestEntity.add("addorsub", "0");
		requestEntity.add("order_code", orderCode);

		try {
			result = restTemplate.postForObject(url, requestEntity, String.class);
			LOG.info("RESULT=======加钱=========/v1.0/user/account/update" + result);
			JSONObject jsonObject = JSONObject.fromObject(result);
		} catch (RestClientException e) {
			LOG.error("==========/v1.0/user/account/update异常===========" + e);
			
		}
		
		LOG.info("订单状态修改成功===================");

		LOG.info("订单已支付!");
		
		
		//this.updateOrderByOrderCode(orderCode, "1", realChannelOrderCode);
 		return "SUCCESS";
		
		
	       
	}
	
	
	
	@RequestMapping(method = RequestMethod.GET, value = "/v1.0/paymentchannel/topup/rechargetoaccount/topage")
	public String toRechargetoAccount(HttpServletRequest request, HttpServletResponse response, Model model)
			throws IOException {
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");
		String from = request.getParameter("from");
		LOG.info("/v1.0/paymentchannel/topup/rechargetoaccount/topage=========" + from);
		//model.addAttribute("bankName", from);
		from = URLDecoder.decode(from, "UTF-8");
		try {
			response.setContentType("text/html;charset=utf-8");
			response.getWriter().println(from);
			response.getWriter().flush();
			response.getWriter().close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
}
