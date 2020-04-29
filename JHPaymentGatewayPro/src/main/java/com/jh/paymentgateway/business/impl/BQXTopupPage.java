package com.jh.paymentgateway.business.impl;

import java.util.HashMap;
import java.util.Map;

import org.hibernate.annotations.SQLUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jh.paymentgateway.business.TopupRequestBusiness;
import com.jh.paymentgateway.controller.BQXpageRequest;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;

@Service
public class BQXTopupPage implements TopupRequestBusiness {

	private static final Logger LOG = LoggerFactory.getLogger(BQXTopupPage.class);

	@Autowired
	BQXpageRequest bqxpageRequest;

	@Override
	public Map<String, Object> topupRequest(Map<String, Object> params) throws Exception {
		PaymentRequestParameter bean = (PaymentRequestParameter) params.get("paymentRequestParameter");

		String orderCode = bean.getOrderCode();
		LOG.info("订单号：" + orderCode);

		Map<String, Object> map = new HashMap<String, Object>();
		LOG.info("判断进入博淇无卡消费------------");
		map = (Map<String, Object>) bqxpageRequest.register(orderCode);
		
		return map;
	}

}
