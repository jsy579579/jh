package com.jh.paymentgateway.business.impl;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jh.paymentgateway.basechannel.BaseChannel;
import com.jh.paymentgateway.business.TopupRequestBusiness;
import com.jh.paymentgateway.controller.LMpageRequest;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;

@Service
public class LMTopupPage extends BaseChannel implements TopupRequestBusiness {

	private static final Logger LOG = LoggerFactory.getLogger(LMTopupPage.class);
	@Autowired
	LMpageRequest lmpageRequest;

	@Override
	public Map<String, Object> topupRequest(Map<String, Object> params) throws Exception {
		PaymentRequestParameter bean = (PaymentRequestParameter) params.get("paymentRequestParameter");

		String orderCode = bean.getOrderCode();
		LOG.info("订单号：" + orderCode);

		Map<String, Object> maps = new HashMap<String, Object>();

		LOG.info("根据判断进入消费任务======");
		maps = (Map<String, Object>) lmpageRequest.Register(orderCode);
		return maps;
	}

}
