package com.jh.paymentgateway.business.impl;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
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
import com.jh.paymentgateway.controller.LDpageRequest;
import com.jh.paymentgateway.pojo.LDRegister;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;
import com.jh.paymentgateway.util.Util;


@Service
public class LDTopupRequest extends BaseChannel implements TopupRequestBusiness {

	private static final Logger LOG = LoggerFactory.getLogger(LDTopupRequest.class);

	
	@Autowired
	private HttpServletRequest request;

	@Autowired
	private LDpageRequest ldpageRequest;

	@Value("${payment.ipAddress}")
	private String ipAddress;

	@Override
	public Map<String, Object> topupRequest(Map<String, Object> params) throws Exception {
		PaymentRequestParameter bean = (PaymentRequestParameter) params.get("paymentRequestParameter");
		String orderCode = bean.getOrderCode();
		Map<String, Object> map = new HashMap<String, Object>();
	
		

		LOG.info("判断进入消费任务==============");
	
		map = (Map<String, Object>) ldpageRequest.ldRegister(request, orderCode);

		return map;
	}

}

