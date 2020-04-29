package com.jh.paymentgateway.basechannel;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.jh.paymentgateway.async.AsyncMethod;
import com.jh.paymentgateway.business.OrderParameterBusiness;
import com.jh.paymentgateway.config.RedisUtil;
import com.jh.paymentgateway.config.RestTemplateUtil;
import com.jh.paymentgateway.pojo.OrderParameter;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;
import com.jh.paymentgateway.util.Util;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;
import net.sf.json.JSONObject;

public class BaseChannel {

	@Autowired
	private Util util;

	@Autowired
	RestTemplate restTemplate;
	
	@Autowired
	AsyncMethod asyncMethod;
	
	@Autowired
	private RedisUtil redisUtil;
	
	@Autowired
	public OrderParameterBusiness orderParameterBusiness;
	
	private final Logger LOG = LoggerFactory.getLogger(getClass());
	
	
	// 将信用卡有效期格式转换为MMYY
	public String expiredTimeToMMYY(String expiredTime) {

		try {
			if(expiredTime.contains("/") || expiredTime.contains("-")) {
				expiredTime = expiredTime.replace("/", "");
				expiredTime = expiredTime.replace("-", "");
			}
			
			String before = expiredTime.substring(0, 2);
			String after = expiredTime.substring(2, 4);

			BigDecimal big = new BigDecimal(before);
			BigDecimal times = new BigDecimal("12");

			int compareTo = big.compareTo(times);
			// 如果前两位大于12，,代表是年/月的格式
			if (compareTo == 1) {
				expiredTime = after + before;
			} else {
				expiredTime = before + after;
			}
		} catch (Exception e) {
			LOG.error("转换有效期格式有误=======" + e);
			e.printStackTrace();
			return expiredTime;
		}

		return expiredTime;
	}

	// 将信用卡有效期格式转换为YYMM
	public String expiredTimeToYYMM(String expiredTime) {

		try {
			if(expiredTime.contains("/") || expiredTime.contains("-")) {
				expiredTime = expiredTime.replace("/", "");
				expiredTime = expiredTime.replace("-", "");
			}
			
			String before = expiredTime.substring(0, 2);
			String after = expiredTime.substring(2, 4);

			BigDecimal big = new BigDecimal(before);
			BigDecimal times = new BigDecimal("12");

			int compareTo = big.compareTo(times);
			// 如果前两位大于12，,代表是年/月的格式
			if (compareTo == 1) {
				expiredTime = before + after;
			} else {
				expiredTime = after + before;
			}
		} catch (Exception e) {
			LOG.error("转换有效期格式有误=======" + e);
			e.printStackTrace();
			return expiredTime;
		}

		return expiredTime;
	}

	
	public void addOrderCauseOfFailure(String orderCode, String remark, String ipAddress){
		
		RestTemplate rt = new RestTemplate();
		
		String url = ipAddress + "/v1.0/transactionclear/payment/update/remark";
		MultiValueMap<String,String> multiValueMap = new LinkedMultiValueMap<String, String>();
		multiValueMap.add("ordercode", orderCode);
		multiValueMap.add("remark", remark);
		String result = rt.postForObject(url, multiValueMap, String.class);
		
	}
	
	public void updatePaymentOrderThirdOrder(String ipAddress, String orderCode,String thirdOrderCode) {
		try {
			RestTemplate restTemplate = new RestTemplate();
			String url = ipAddress + "/v1.0/transactionclear/payment/update/thirdordercode";
			MultiValueMap<String,String> multiValueMap = new LinkedMultiValueMap<String, String>();
			multiValueMap.add("order_code", orderCode);
			multiValueMap.add("third_code", thirdOrderCode);
			String result = restTemplate.postForObject(url, multiValueMap, String.class);
		} catch (RestClientException e) {
			e.printStackTrace();
		}
	}

	public void updateSuccessPaymentOrder(String ipAddress,String orderCode) {
		asyncMethod.updateSuccessPaymentOrder(ipAddress, orderCode);
	}
	
	public void updateSuccessPaymentOrder(String ipAddress,String orderCode,String third_code) {
		asyncMethod.updateSuccessPaymentOrder(ipAddress, orderCode,third_code);
	}
	public void updateStatusPaymentOrder(String ipAddress,String status,String orderCode,String third_code) {
		asyncMethod.updateStatusPaymentOrder(ipAddress, status, orderCode, third_code);
	}
	/**
	 * 身份证最后尾X转大写
	 * */
	public String idCardLastToUppercase(String idCard) {
		String last = idCard.substring(idCard.length()-1);
		String delLast = idCard.substring(0,idCard.length()-1);
		String toUppercase = null;
		if ("x".equals(last)) {
			LOG.info("身份证最后一位转大写字母");
			toUppercase = delLast+"X";
			LOG.info(toUppercase);
		} else {
			LOG.info("身份证最后一位已经是大写，或身份证无字母");
			toUppercase = idCard;
			LOG.info(toUppercase);
		}
		return toUppercase;
	}
	
	/**
	 * 身份证最后尾X转小写
	 * */
	public String idCardLastToLowercase(String idCard) {
		String last = idCard.substring(idCard.length()-1);
		String delLast = idCard.substring(0,idCard.length()-1);
		String toUppercase = null;
		if ("X".equals(last)) {
			LOG.info("身份证最后一位转小写");
			toUppercase =  delLast+"x";
			LOG.info(toUppercase);
		} else {
			LOG.info("身份证最后一位已经是小写，或身份证无字母");
			toUppercase = idCard;
			LOG.info(toUppercase);
		}
		return toUppercase;
	}
	
	
	public void addWhetherToRequest(String orderCode, int whetherToRequest) {
		
		OrderParameter findByOrderCode = orderParameterBusiness.findByOrderCode(orderCode);
		findByOrderCode.setWhetherToRequest(whetherToRequest);
		orderParameterBusiness.save(findByOrderCode);
	};
	
	
	public String whetherRepeatOrder(String orderCode) {
		
		String paymentOrder = null;
		PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
		OrderParameter findByOrderCode = orderParameterBusiness.findByOrderCode(orderCode);
		
		if(findByOrderCode.getWhetherToRequest() == 1) {
			LOG.info("queryWhetherToRequestByOrderCode为1代表订单号：" + orderCode +" 请求过接口,需要重新生成订单======");
			
			RestTemplate restTemplate = new RestTemplate();
			String url = prp.getIpAddress() + "/v1.0/transactionclear/payment/query/ordercode";
			MultiValueMap<String,String> multiValueMap = new LinkedMultiValueMap<String, String>();
			multiValueMap.add("order_code", orderCode);
			String result = restTemplate.postForObject(url, multiValueMap, String.class);
			
			JSONObject jsonObject = JSONObject.fromObject(result);
			String respCode = jsonObject.getString(CommonConstants.RESP_CODE);
			
			if(CommonConstants.SUCCESS.equals(respCode)) {
				JSONObject json = jsonObject.getJSONObject(CommonConstants.RESULT);
				LOG.info("json======" + json);
				
				String brandId = json.getString("brandid");
				
				url = prp.getIpAddress() + "/v1.0/transactionclear/payment/add";

				MultiValueMap<String,String> requestEntity = new LinkedMultiValueMap<String, String>();
				requestEntity.add("type", "0");
				requestEntity.add("phone", prp.getPhone());
				requestEntity.add("amount", prp.getAmount());
				requestEntity.add("openid", "");
				requestEntity.add("channel_tag", prp.getChannelTag());
				requestEntity.add("desc", prp.getExtra());
				requestEntity.add("remark", "");
				requestEntity.add("bank_card", prp.getBankCard());
				requestEntity.add("brand_id", brandId);
				
				result = restTemplate.postForObject(url, requestEntity, String.class);
				LOG.info("接口/v1.0/transactionclear/payment/add--RESULT================" + result);
				jsonObject = JSONObject.fromObject(result);
				
				JSONObject resultObj = jsonObject.getJSONObject("result");
				String order = resultObj.getString("ordercode");
			
				JSONObject jsonObj = new JSONObject();
				
				jsonObj.put("orderCode", order);
				jsonObj.put("amount", prp.getAmount());
				jsonObj.put("bankCard", prp.getBankCard());
				jsonObj.put("realAmount", prp.getRealAmount());
				jsonObj.put("userId", prp.getUserId());
				jsonObj.put("rate", prp.getRate());
				jsonObj.put("extraFee", prp.getExtraFee());
				jsonObj.put("userName", prp.getUserName());
				jsonObj.put("idCard", prp.getIdCard());
				jsonObj.put("creditCardPhone", prp.getCreditCardPhone());
				jsonObj.put("creditCardBankName", prp.getCreditCardBankName());
				jsonObj.put("creditCardNature", prp.getCreditCardNature());
				jsonObj.put("creditCardCardType", prp.getCreditCardCardType());
				jsonObj.put("expiredTime", prp.getExpiredTime());
				jsonObj.put("securityCode", prp.getSecurityCode());
				jsonObj.put("debitCardNo", prp.getDebitCardNo());
				jsonObj.put("debitPhone", prp.getDebitPhone());
				jsonObj.put("debitBankName", prp.getDebitBankName());
				jsonObj.put("debitCardNature", prp.getDebitCardNature());
				jsonObj.put("debitCardCardType", prp.getDebitCardCardType());
				jsonObj.put("channelTag", prp.getChannelTag());
				jsonObj.put("orderType", prp.getOrderType());
				jsonObj.put("extra", prp.getExtra());
				jsonObj.put("phone", prp.getPhone());
				jsonObj.put("orderDesc", prp.getExtra());
				jsonObj.put("ipAddress", prp.getIpAddress());
				
				String extra = jsonObj.getString("extra");
				
				PaymentRequestParameter bean = (PaymentRequestParameter) JSONObject.toBean(jsonObj, PaymentRequestParameter.class);
				bean.setExtra(extra);
				redisUtil.savePaymentRequestParameter(order, bean);
				
				this.addWhetherToRequest(order, 1);
				
				paymentOrder = order;
				
			}
		}else {
			
			this.addWhetherToRequest(orderCode, 1);
			
			paymentOrder = orderCode;
		}
		
		return paymentOrder;
	}
	
}
