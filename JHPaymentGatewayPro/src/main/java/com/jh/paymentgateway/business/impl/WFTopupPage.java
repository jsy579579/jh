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
import com.jh.paymentgateway.controller.WFpageRequest;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;
import com.jh.paymentgateway.pojo.WFRegister;
import com.jh.paymentgateway.util.Util;

@Service
public class WFTopupPage extends BaseChannel implements  TopupRequestBusiness{

	private static final Logger LOG = LoggerFactory.getLogger(WFTopupPage.class);

	@Autowired
	private Util util;
	
	@Value("${payment.ipAddress}")
	private String ip;
	
	@Autowired
	private TopupPayChannelBusiness topupPayChannelBusiness;
	
	@Autowired
	private WFpageRequest wfpageRequest;
	
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
		
		Map<String,Object> map = new HashMap<String, Object>();
		
		WFRegister wfRegister = topupPayChannelBusiness.getWFRegisterByIdCard(idCard);
		
		if("10".equals(orderType)) {
			LOG.info("根据判断进入消费任务======");
			
			if(!rate.equals(wfRegister.getRate()) || !extraFee.equals(wfRegister.getExtraFee())) {
				
				map = (Map<String, Object>) wfpageRequest.wfRegister(bankCard, idCard, phone, userName, bankName, rate, extraFee, "1");
				Object respCode = map.get("resp_code");
				Object respMessage = map.get("resp_message");
				LOG.info("respCode====="+respCode);
				
				if("000000".equals(respCode)) {
					
					map = (Map<String, Object>) wfpageRequest.wfFastPay(orderCode);
					
				}
				
				
			}else {
				
				map = (Map<String, Object>) wfpageRequest.wfFastPay(orderCode);
				
			}
			
		}
			
		
		if("11".equals(orderType)) {
			
			map = (Map<String, Object>) wfpageRequest.wfTransfer(orderCode);
			
		}
		
		return map;
	}

}
