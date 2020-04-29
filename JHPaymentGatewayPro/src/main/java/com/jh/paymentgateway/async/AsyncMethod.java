package com.jh.paymentgateway.async;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.jh.paymentgateway.common.ChannelUtils;

@Component
@Lazy(true)
public class AsyncMethod{

	private final Logger LOG = LoggerFactory.getLogger(getClass());

	@Async
	public void updateSuccessPaymentOrder(String ipAddress,String orderCode) {

		String url = ipAddress+ChannelUtils.getCallBackUrl(ipAddress);
		//String url = ipAddress + "/v1.0/transactionclear/payment/update";
		MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("status", "1");
		requestEntity.add("order_code", orderCode);
		requestEntity.add("third_code", "");
		String result = null;
		try {
			result = new RestTemplate().postForObject(url, requestEntity, String.class);
		} catch (Exception e1) {
			LOG.error("=====" + orderCode + "=====更新订单异常",e1);
			return;
		}
		LOG.info("订单状态修改成功===================" + orderCode + "====================" + result);	}

	@Async
	public void updateSuccessPaymentOrder(String ipAddress,String orderCode,String third_code) {

		Map<String, Object> maps = new HashMap<String, Object>();

		RestTemplate restTemplate = new RestTemplate();
		MultiValueMap<String, Object> requestEntity = new LinkedMultiValueMap<String, Object>();
		String url = null;
		String result = null;
		url=ipAddress+ChannelUtils.getCallBackUrl(ipAddress);
		//测试地址
		//url="http://localhost/v1.0/transactionclear/payment/update";
		requestEntity = new LinkedMultiValueMap<String, Object>();
		requestEntity.add("status", "1");
		requestEntity.add("order_code", orderCode);
		requestEntity.add("third_code", third_code);
		//requestEntity.add("third_code", "代码400");
		try {
			result = restTemplate.postForObject(url, requestEntity, String.class);
		} catch (Exception e) {
			e.printStackTrace();
			LOG.info("================================更新余额体现订单异常");
		}
		LOG.info("========================"+orderCode+"================订单更新成功!");
		
		}
//		String url = ipAddress+ChannelUtils.getCallBackUrl(ipAddress);
//		//String url = ipAddress + "/v1.0/transactionclear/payment/update";
//		MultiValueMap<String, Object> requestEntity = new LinkedMultiValueMap<String, Object>();
//		requestEntity.add("status", "1");
//		requestEntity.add("order_code", orderCode);
//		requestEntity.add("third_code", third_code);
//		String result = null;
//		try {
//			result = new RestTemplate().postForObject(url, requestEntity, String.class);
//		} catch (Exception e1) {
//			LOG.error("=====" + orderCode + "=====更新订单异常",e1);
//			return;
//		}
//		LOG.info("订单状态修改成功===================" + orderCode + "====================" + result);	}

	@Async
	public void updateStatusPaymentOrder(String ipAddress,String status,String orderCode,String third_code) {

		String url = ipAddress+ChannelUtils.getCallBackUrl(ipAddress);
		//String url = ipAddress + "/v1.0/transactionclear/payment/update";
		MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
		requestEntity.add("status", status);
		requestEntity.add("order_code", orderCode);
		requestEntity.add("third_code", third_code);
		String result = null;
		try {
			result = new RestTemplate().postForObject(url, requestEntity, String.class);
		} catch (Exception e1) {
			LOG.error("=====" + orderCode + "=====更新订单异常",e1);
			return;
		}
		LOG.info("订单状态修改成功===================" + orderCode + "====================" + result);	}

}
