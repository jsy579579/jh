package com.jh.paymentgateway.business.impl;

import com.jh.paymentgateway.basechannel.BaseChannel;
import com.jh.paymentgateway.business.TopupPayChannelBusiness;
import com.jh.paymentgateway.business.TopupRequestBusiness;
import com.jh.paymentgateway.controller.HQNEWpageRequest;
import com.jh.paymentgateway.controller.HQpageRequest;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;
import com.jh.paymentgateway.pojo.hq.HQNEWBindCard;
import com.jh.paymentgateway.pojo.hq.HQNEWRegister;
import com.jh.paymentgateway.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class HQNEWTopPage extends BaseChannel implements TopupRequestBusiness {

	private static final Logger LOG = LoggerFactory.getLogger(HQNEWTopPage.class);

	@Autowired
	private Util util;

	@Value("${payment.ipAddress}")
	private String ip;

	@Autowired
	private TopupPayChannelBusiness topupPayChannelBusiness;

	@Autowired
	private HQNEWpageRequest hqnewPageRequest;

	@Override
	public Map<String, Object> topupRequest(Map<String, Object> params) throws Exception {
		PaymentRequestParameter bean = (PaymentRequestParameter) params.get("paymentRequestParameter");
		String bankCard = bean.getBankCard();
		String phone = bean.getPhone();
		String idCard = bean.getIdCard();
		String userName = bean.getUserName();
		String rate = bean.getRate();
		String extraFee = bean.getExtraFee();
		String orderCode = bean.getOrderCode();
		String orderType = bean.getOrderType();
		String securityCode = bean.getSecurityCode();
		String expiredTime = bean.getExpiredTime();
		LOG.info("qhnew订单号========================" + orderCode);
		Map<String, Object> maps = new HashMap();
		HQNEWBindCard hqnewbind = topupPayChannelBusiness.getHQNEWBindCardByBankCard(bankCard);
		HQNEWRegister hqnewregister = topupPayChannelBusiness.getHQNEWRegisterByIdCard(idCard);
		String expireDate = this.expiredTimeToMMYY(expiredTime);
		if ("10".equals(orderType)) {
			LOG.info("根据判断进入消费任务======");
			// 判断用户是否注册
			if (hqnewregister == null) {
				maps = (Map<String, Object>) hqnewPageRequest.hqRegister(bankCard, idCard, phone, userName, rate,
						extraFee);
				if (!"000000".equals(maps.get("resp_code"))) {
					return maps;
				}
			}
			// 判断用户是否绑卡
			if (hqnewbind == null || "0".equals(hqnewbind.getStatus())) {
				maps = (Map<String, Object>) hqnewPageRequest.hqBindCard(bankCard, idCard, phone, userName,
						securityCode, expireDate);
				if (!"000000".equals(maps.get("resp_code"))) {
					return maps;
				}
			}
			// 判断用户是否修改费率或单笔手续费
			if (!rate.equals(hqnewregister.getRate()) | !extraFee.equals(hqnewregister.getExtraFee())) {
				maps = (Map<String, Object>) hqnewPageRequest.changeRate(bankCard, rate, extraFee, idCard);
				if (!"000000".equals(maps.get("resp_code"))) {
					return maps;
				}
			}
			// 用户进入消费任务
			maps = (Map<String, Object>) hqnewPageRequest.topay(orderCode);
		}

		if ("11".equals(orderType)) {
			// 用户进入还款任务
			maps = (Map<String, Object>) hqnewPageRequest.transfer(orderCode);
		}
		return maps;
	}
}
