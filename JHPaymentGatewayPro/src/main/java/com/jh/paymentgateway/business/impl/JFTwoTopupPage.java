package com.jh.paymentgateway.business.impl;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jh.paymentgateway.basechannel.BaseChannel;
import com.jh.paymentgateway.business.TopupPayChannelBusiness;
import com.jh.paymentgateway.business.TopupRequestBusiness;
import com.jh.paymentgateway.controller.JFTwopageRequset;
import com.jh.paymentgateway.controller.JFXpageRequset;
import com.jh.paymentgateway.controller.JFpageRequset;
import com.jh.paymentgateway.pojo.JFBindCard;
import com.jh.paymentgateway.pojo.JFRegister;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;

@Service
public class JFTwoTopupPage extends BaseChannel implements TopupRequestBusiness {

	private static final Logger LOG = LoggerFactory.getLogger(JFTwoTopupPage.class);
	@Autowired
	private TopupPayChannelBusiness topupPayChannelBusiness;

	@Autowired
	private JFTwopageRequset jFTwopageRequset;

	@Override
	public Map<String, Object> topupRequest(Map<String, Object> params) throws Exception {
		PaymentRequestParameter bean = (PaymentRequestParameter) params.get("paymentRequestParameter");

		String orderCode = bean.getOrderCode();
		LOG.info("订单号："+orderCode);

		Map<String, Object> map = new HashMap<String, Object>();

		/*JFBindCard jfBindCard = topupPayChannelBusiness.getJFBindCardByBankCard(bankCard);
		JFRegister jfRegister = topupPayChannelBusiness.getJFRegisterByIdCard(idCard);*/
			
			
			LOG.info("根据判断进入消费任务======");
			map = (Map<String, Object>) jFTwopageRequset.register(orderCode);

		return map;
	}
}