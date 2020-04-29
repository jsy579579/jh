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
import com.jh.paymentgateway.controller.YFJRpageRequest;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;
import com.jh.paymentgateway.pojo.YFJRBinkCard;
import com.jh.paymentgateway.pojo.YFJRRegister;
import com.jh.paymentgateway.util.Util;

@Service
public class YFJRTopupPage extends BaseChannel implements TopupRequestBusiness{

	private static final Logger LOG = LoggerFactory.getLogger(YFJRTopupPage.class);

	@Autowired
	private Util util;
	
	@Value("${payment.ipAddress}")
	private String ip;
	 
	@Autowired
	private TopupPayChannelBusiness topupPayChannelBusiness;
	
	@Autowired
	private YFJRpageRequest yFJRpageRequest;
	
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
		String headBank = bean.getCreditCardBankName();
		String cardType = bean.getCreditCardCardType();
		
		// 百分制
		String bigRate = new BigDecimal(transRate).multiply(new BigDecimal("100")).setScale(2).toString();
		
		Map<String,Object> map = new HashMap<String, Object>();
		
		YFJRRegister yFJRRegister = topupPayChannelBusiness.getYFJRRegisterByIdNum(settleIdNum);
		
		YFJRBinkCard yFJRBinkCard = topupPayChannelBusiness.getYFJRBinkCardByIdNum(settleIdNum,settleNum,"1");//查询卡是否已绑定
		String bankCard = "";
    	if (yFJRBinkCard!=null) {
    		bankCard = yFJRBinkCard.getBankCard();
		}
		
		if("0".equals(orderType)) {
			LOG.info("判断进入消费任务--------");
			
			if(yFJRRegister==null){
				LOG.info("用户需要进件--------");
				
				map = (Map<String, Object>) yFJRpageRequest.register(settleName,userId,settlePhone,settleIdNum,settleNum,headBank,cardType);
				
				
			}else if(yFJRBinkCard==null){  
				LOG.info("用户需要绑卡--------");
				
				map = (Map<String, Object>) yFJRpageRequest.bindCard(settleName,settleIdNum,settleNum,acct_cvv2,acct_validdate,settlePhone,headBank);

			}else {
				LOG.info("直接进入交易--------");
				map = (Map<String, Object>) yFJRpageRequest.pay(orderCode);
				
			}
		}
			
		
		if("11".equals(orderType)) {
			LOG.info("进入还款--------");
			map = (Map<String, Object>) yFJRpageRequest.payment(orderCode);
			
		}
		
		return map;
	}

}
