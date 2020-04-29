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
import com.jh.paymentgateway.controller.RYTpageRequest;
import com.jh.paymentgateway.pojo.CJHKRegister;
import com.jh.paymentgateway.pojo.CJXChannelCode;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;
import com.jh.paymentgateway.pojo.RYTBindCard;
import com.jh.paymentgateway.pojo.RYTRegister;
import com.jh.paymentgateway.util.Util;

import cn.jh.common.utils.CommonConstants;

@Service
public class RYTTopupPage extends BaseChannel implements TopupRequestBusiness {

	private static final Logger LOG = LoggerFactory.getLogger(RYTTopupPage.class);

	@Autowired
	private HttpServletRequest request;

	@Autowired
	private RYTpageRequest rytPageRequest;

	@Value("${payment.ipAddress}")
	private String ipAddress;

	@Autowired
	private TopupPayChannelBusiness topupPayChannelBusiness;

	@Override
	public Map<String, Object> topupRequest(Map<String, Object> params) throws Exception {
		PaymentRequestParameter bean = (PaymentRequestParameter) params.get("paymentRequestParameter");
		Map<String, Object> map = new HashMap<String, Object>();
		Map<String, String> mapr = new HashMap<String, String>();
		String orderCode = bean.getOrderCode();
		String orderType = bean.getOrderType();
		String bankNo = bean.getBankCard();
		String rate = bean.getRate();
		String idCard = bean.getIdCard();
		RYTRegister rytRegister= topupPayChannelBusiness.getRYTRegisterByIdcard(idCard);
		RYTBindCard rytBindCard = topupPayChannelBusiness.getRYTBindCardByBankCard(bankNo);
		if(rytRegister!=null&&rytBindCard!=null&&rytRegister.getStatus().equals("1")&&rytBindCard.getStatus().equals("1")){
			if(!rytRegister.getRate().equals(rate)){
				mapr=rytPageRequest.CreditRHChangeRate(rytRegister.getMerchantCode(), rate);
				String code = mapr.get("respCode");
				String message = mapr.get("respMsg");
				if(code.equals("0000")){
					rytRegister.setRate(rate);
					topupPayChannelBusiness.createRYTRegister(rytRegister);
				}else{
					map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
					map.put(CommonConstants.RESP_MESSAGE, message);
					this.addOrderCauseOfFailure(orderCode, message, bean.getIpAddress());
				}
			}
			
			if ("10".equals(orderType)) {
				LOG.info("判断进入消费任务==============");
				map=(Map<String, Object>)rytPageRequest.rytConsume(request, orderCode);
			}
			if ("11".equals(orderType)) {
				LOG.info("根据判断进入还款任务======");
				map = (Map<String, Object>) rytPageRequest.rytAdvance(request, orderCode);
			}
		}
		

		return map;
	}
}
