package com.jh.paymentgateway.business.impl;

import java.math.BigDecimal;
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
import com.jh.paymentgateway.controller.RHJFpageRequest;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;
import com.jh.paymentgateway.pojo.RHJFBindCard;
import com.jh.paymentgateway.pojo.RHJFRegister;
import com.jh.paymentgateway.util.Util;

@Service
public class RHJFTopupPage extends BaseChannel implements TopupRequestBusiness {

	private static final Logger LOG = LoggerFactory.getLogger(RHJFTopupPage.class);

	@Autowired
	private Util util;

	@Value("${payment.ipAddress}")
	private String ip;

	@Autowired
	private TopupPayChannelBusiness topupPayChannelBusiness;

	@Autowired
	private RHJFpageRequest rhjfpageRequest;

	@Autowired
	private HttpServletRequest request;

	@Override
	public Map<String, Object> topupRequest(Map<String, Object> params) throws Exception {
		PaymentRequestParameter bean = (PaymentRequestParameter) params.get("paymentRequestParameter");

		String transType = "MERCHANT_WITHDRAWQUERY";
		String orderCode = bean.getOrderCode();
		String bankCard = bean.getBankCard();
		String userName = bean.getUserName();
		String bankName = bean.getCreditCardBankName();
		String phone = bean.getCreditCardPhone();
		String idCard = bean.getIdCard();
		String orderType = bean.getOrderType();
		String securityCode = bean.getSecurityCode();
		String rate = bean.getRate();

		// 百分制
		String bigRate = new BigDecimal(rate).multiply(new BigDecimal("100")).setScale(2).toString();
		
		Map<String, Object> map = new HashMap<String, Object>();

		RHJFRegister rhjfRegister = topupPayChannelBusiness.getRHJFRegisterByIdCard(idCard);
		RHJFBindCard rhjfBindCard = topupPayChannelBusiness.getRHJFBindCardByBankCard(bankCard,"1");
		if ("0".equals(orderType)) {
			LOG.info("判断进入消费任务--------");
			
			if (rhjfRegister==null) {
				LOG.info("用户需要进件--------");
				map = (Map<String, Object>) rhjfpageRequest.rhjfRegister(bankCard, idCard, phone, userName, bankName);
				Object respCode = map.get("resp_code");;
				if ("000000".equals(respCode)) {
					map = (Map<String, Object>) rhjfpageRequest.rhjfopenProduct(idCard,rate);
				}
			}else if(!bigRate.equals(rhjfRegister.getRate())){
				LOG.info("修改费率--------");
				
				map = (Map<String, Object>) rhjfpageRequest.rhjfchangeMerChantRate(bigRate, idCard);
			}else if(rhjfBindCard==null){
				LOG.info("用户需要绑卡--------");
				
				map = (Map<String, Object>) rhjfpageRequest.rhjfBindCard(bankCard, idCard, phone, securityCode);
			}else{
				LOG.info("直接进入交易--------");
				map = (Map<String, Object>) rhjfpageRequest.rhjfFastPay(request, orderCode);
			}
		}
		
		if("11".equals(orderType)) {
			LOG.info("进入还款--------");
			
			map = (Map<String, Object>) rhjfpageRequest.rhjfTransfer(orderCode);	
		}
		return map;

	}
}
	
