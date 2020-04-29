package com.jh.paymentgateway.business.impl;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.jh.paymentgateway.basechannel.BaseChannel;
import com.jh.paymentgateway.business.TopupRequestBusiness;
import com.jh.paymentgateway.controller.MHHQEpageRequest;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;

@Service
public class MHHQETopupPage extends BaseChannel implements  TopupRequestBusiness{

	private static final Logger LOG = LoggerFactory.getLogger(MHHQETopupPage.class);

	@Value("${payment.ipAddress}")
	private String ip;
	
	@Autowired
	private MHHQEpageRequest hqePageRequst;
	
	@Override
	public Map<String, Object> topupRequest(Map<String, Object> params) throws Exception {
		PaymentRequestParameter bean = (PaymentRequestParameter) params.get("paymentRequestParameter");
		
		String orderCode = bean.getOrderCode();
		String orderType = bean.getOrderType();
		String orderDesc = bean.getExtra();
		
		Map<String,Object> map = new HashMap<String, Object>();
		
		String storeNo = "310200";
		try {
			if(orderDesc.contains("M")) {
				
				storeNo = orderDesc.substring(orderDesc.indexOf("M")+1, orderDesc.length());
			}else {
				
				if(orderDesc.contains("-")) {
					
					storeNo = "310200";
				}
			}
		} catch (Exception e) {
			LOG.error("获取城市编码有误======",e);
			
			storeNo = "310200";
		}
		
		
		if("10".equals(orderType)) {
			LOG.info("根据判断进入消费任务======");
			
			map = (Map<String, Object>) hqePageRequst.hqePreFastPay(orderCode, storeNo);
			
		}
		
		
		if("11".equals(orderType)) {
			
			map = (Map<String, Object>) hqePageRequst.transfer(orderCode);
			
		}
		
		return map;
	}

}
