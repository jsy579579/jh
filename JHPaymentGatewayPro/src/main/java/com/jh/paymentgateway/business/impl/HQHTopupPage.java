package com.jh.paymentgateway.business.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.jh.paymentgateway.basechannel.BaseChannel;
import com.jh.paymentgateway.business.TopupPayChannelBusiness;
import com.jh.paymentgateway.business.TopupRequestBusiness;
import com.jh.paymentgateway.controller.CJHKXpageRequest;
import com.jh.paymentgateway.controller.HQHpageRequest;
import com.jh.paymentgateway.pojo.CJHKRegister;
import com.jh.paymentgateway.pojo.CJXChannelCode;
import com.jh.paymentgateway.pojo.HQGBindCard;
import com.jh.paymentgateway.pojo.HQGRegister;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;
import com.jh.paymentgateway.pojo.RYTBindCard;
import com.jh.paymentgateway.pojo.RYTRegister;
import com.jh.paymentgateway.util.Util;

import cn.jh.common.utils.CommonConstants;

@Service
public class HQHTopupPage extends BaseChannel implements TopupRequestBusiness {

	private static final Logger LOG = LoggerFactory.getLogger(HQHTopupPage.class);

	@Autowired
	private HttpServletRequest request;

	@Autowired
	private HQHpageRequest hqhpageRequest;

	@Value("${payment.ipAddress}")
	private String ipAddress;

	@Autowired
	private TopupPayChannelBusiness topupPayChannelBusiness;

	@Override
	public Map<String, Object> topupRequest(Map<String, Object> params) throws Exception {
		PaymentRequestParameter bean = (PaymentRequestParameter) params.get("paymentRequestParameter");
		Map<String, Object> map = new HashMap<String, Object>();
		String orderCode = bean.getOrderCode();
		String orderType = bean.getOrderType();
		if ("10".equals(orderType)) {
			LOG.info("判断进入消费任务==============");
			map=(Map<String, Object>)hqhpageRequest.fastpay(request, orderCode);
		}
		if ("11".equals(orderType)) {
			LOG.info("根据判断进入还款任务======");
			map = (Map<String, Object>) hqhpageRequest.transfer(request, orderCode);
		}
		return map;
	}
}
