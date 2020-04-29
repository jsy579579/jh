package com.jh.paymentgateway.business.impl;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.jh.paymentgateway.basechannel.BaseChannel;
import com.jh.paymentgateway.business.TopupPayChannelBusiness;
import com.jh.paymentgateway.business.TopupRequestBusiness;
import com.jh.paymentgateway.controller.HQBpageRequest;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;
import com.jh.paymentgateway.util.Util;

import cn.jh.common.tools.ResultWrap;

@Service
public class HQBTopupPage extends BaseChannel implements  TopupRequestBusiness{

	private static final Logger LOG = LoggerFactory.getLogger(HQBTopupPage.class);

	@Autowired
	private Util util;
	
	@Value("${payment.ipAddress}")
	private String ip;
	
	@Autowired
	private TopupPayChannelBusiness topupPayChannelBusiness;
	
	@Autowired
	private HQBpageRequest hqbpageRequst;
	
	@Override
	public Map<String, Object> topupRequest(Map<String, Object> params) throws Exception {
		PaymentRequestParameter bean = (PaymentRequestParameter) params.get("paymentRequestParameter");
		
		String orderCode = bean.getOrderCode();
		String bankCard = bean.getBankCard();
		String bankName = bean.getCreditCardBankName();
		String userName = bean.getUserName();
		String phone = bean.getCreditCardPhone();
		String ipAddress = bean.getIpAddress();
		String rate = bean.getRate();
		String extraFee = bean.getExtraFee();
		String idCard = bean.getIdCard();
		String orderType = bean.getOrderType();
		String extra = bean.getExtra();
		
		Map<String,Object> map = new HashMap<String, Object>();
		
		
		if("10".equals(orderType)) {
			LOG.info("根据判断进入消费任务======");
			
			return ResultWrap.init("999998", "请求成功");
			
		}
		
		
		if("11".equals(orderType)) {
			LOG.info("根据判断进入创建还款计划======");
			
			map = (Map<String, Object>) hqbpageRequst.createRepayPlan(extra, bankCard, bankName, userName, phone, rate, extraFee);
			
		}
		
		return map;
	}

}
