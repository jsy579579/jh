package com.jh.paymentgateway.business.impl;

import java.math.BigDecimal;
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
import com.jh.paymentgateway.controller.BQpageRequest;
import com.jh.paymentgateway.pojo.BQBankCard;
import com.jh.paymentgateway.pojo.BQRegister;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;
import com.jh.paymentgateway.util.Util;

@Service
public class BQTopupPage extends BaseChannel implements TopupRequestBusiness{

	private static final Logger LOG = LoggerFactory.getLogger(BQTopupPage.class);

	@Autowired
	private Util util;
	
	@Value("${payment.ipAddress}")
	private String ip;
	 
	@Autowired
	private TopupPayChannelBusiness topupPayChannelBusiness;
	
	@Autowired
	private BQpageRequest bQpageRequest;
	
	@Override
	public Map<String, Object> topupRequest(Map<String, Object> params) throws Exception {
		PaymentRequestParameter bean = (PaymentRequestParameter) params.get("paymentRequestParameter");
		
		String orderCode = bean.getOrderCode();
		String settleNum = bean.getBankCard();
		String settleName = bean.getUserName();
		String settlePhone = bean.getCreditCardPhone();
		String transRate = bean.getRate();
		String settleIdNum = bean.getIdCard();
		String orderType = bean.getOrderType();
		String userId = bean.getUserId();  
		String acct_cvv2 = bean.getSecurityCode();
		String acct_validdate = bean.getExpiredTime();
		
		// 百分制
		String bigRate = new BigDecimal(transRate).multiply(new BigDecimal("100")).setScale(2).toString();
		
		Map<String,Object> map = new HashMap<String, Object>();
		
		BQRegister bQRegister = topupPayChannelBusiness.getBQRegisterByIdNum(settleIdNum);
		
		BQBankCard bQBankCard = topupPayChannelBusiness.getBQBankCardByIdNum(settleIdNum,settleNum);//绑卡信息
		String bankCard = "";
    	if (bQBankCard!=null) {
    		bankCard = bQBankCard.getAcctNo();
		}
		
		if("10".equals(orderType)) {
			LOG.info("判断进入消费任务--------");
			
			if(bQRegister==null){
				LOG.info("用户需要进件--------");
				
				map = (Map<String, Object>) bQpageRequest.register(settleName,userId,settlePhone,settleIdNum,settleNum,transRate);
				
				
			}else if(!bigRate.equals(bQRegister.getRate())) {
				LOG.info("修改费率,重新进件--------");
				
				map = (Map<String, Object>) bQpageRequest.register(settleName,userId,settlePhone,settleIdNum,settleNum,transRate);
				
			}else if(bQBankCard==null){  
				LOG.info("用户需要绑卡--------");
				
				map = (Map<String, Object>) bQpageRequest.beSign(settleNum, settleName, settlePhone, settleIdNum, acct_cvv2, acct_validdate);
				
/*				Object respCode = map.get("resp_code");
				LOG.info("respCode====="+respCode);
				
				if("000000".equals(respCode)) {
					LOG.info("进入交易--------");
					map = (Map<String, Object>) bQpageRequest.createNoCardOrder(orderCode,acct_cvv2,acct_validdate);
					
				}*/
			}else {
				LOG.info("直接进入交易--------");
				map = (Map<String, Object>) bQpageRequest.createNoCardOrder(orderCode,acct_cvv2,acct_validdate);
				
			}
		}
			
		
		if("11".equals(orderType)) {
			LOG.info("进入还款--------");
			map = (Map<String, Object>) bQpageRequest.creditCardPayment(orderCode);
			
		}
		
		return map;
	}

}
