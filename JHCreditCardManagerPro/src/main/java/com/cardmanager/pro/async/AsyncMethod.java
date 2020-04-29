package com.cardmanager.pro.async;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import net.sf.json.JSONObject;

@Component
@Lazy(true)
public class AsyncMethod{
	
	private final Logger LOG = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private RestTemplate restTemplate;
	
	@Async
	public void updatePaymentOrderByOrderCode(String orderCode){
		//老系统，小米通，水滴信用，聚金宝
		String url = "http://transactionclear/v1.0/transactionclear/payment/update";
		//String url = "http://transactionclear/v1.0/transactionclear/payment/update";
		JSONObject resultJSONObject;
		try {
			LinkedMultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<String, String>();
			requestEntity.add("order_code", orderCode);
			requestEntity.add("status", "1");
			LOG.info("订单====="+orderCode+"=====修改中..........");
			resultJSONObject = restTemplate.postForObject(url,requestEntity,JSONObject.class);
			LOG.info("订单====="+orderCode+"=====修改结果:" + resultJSONObject);
		} catch (Exception e) {
			e.printStackTrace();LOG.error("",e);
			throw new RuntimeException(e);
		}
	}

}
