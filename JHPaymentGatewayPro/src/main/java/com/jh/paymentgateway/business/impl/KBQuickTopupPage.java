package com.jh.paymentgateway.business.impl;

import java.net.URLEncoder;
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
import com.jh.paymentgateway.controller.KBpageRequest;
import com.jh.paymentgateway.pojo.KBBindCard;
import com.jh.paymentgateway.pojo.KBRegister;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;
import com.jh.paymentgateway.util.Util;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;

@Service
public class KBQuickTopupPage extends BaseChannel implements TopupRequestBusiness {

	private static final Logger LOG = LoggerFactory.getLogger(KBQuickTopupPage.class);

	@Autowired
	private Util util;

	@Value("${payment.ipAddress}")
	private String ip;

	@Autowired
	private TopupPayChannelBusiness topupPayChannelBusiness;

	@Autowired
	private KBpageRequest KBpageRequest;

	@Override
	public Map<String, Object> topupRequest(Map<String, Object> params) throws Exception {
		PaymentRequestParameter bean = (PaymentRequestParameter) params.get("paymentRequestParameter");

		String orderCode = bean.getOrderCode();
		String creditCardbankCard = bean.getBankCard();
		String amount = bean.getAmount();
		String expiredTime = bean.getExpiredTime();
		String securityCode = bean.getSecurityCode();
		String idCard = bean.getIdCard();
		String creditCardBankName = bean.getCreditCardBankName();
		String creditCardCardType = bean.getCreditCardCardType();
		String debitCardNo = bean.getDebitCardNo();
		String extra = bean.getExtra();
		String channelTag = bean.getChannelTag();
		
		Map<String, Object> map = new HashMap<String, Object>();

		KBRegister kbRegisterByIdCard = topupPayChannelBusiness.getKBRegisterByIdCard(idCard);
		KBBindCard kbBindCardByBankCard = topupPayChannelBusiness.getKBBindCardByBankCard(creditCardbankCard);

		String productCode = null;
		if("KB_QUICK".equals(channelTag)) {
			productCode = "KB001";
		}
		
		if("KB_QUICK1".equals(channelTag)) {
			productCode = "KB005";
		}
		
		if (kbRegisterByIdCard == null) {
			LOG.info("用户需要进件======");

			map = (Map<String, Object>) KBpageRequest.KBQuickRegister(orderCode, productCode);

			return map;
		} else {
			if (!debitCardNo.equals(kbRegisterByIdCard.getBankCard())) {
				LOG.info("用户需要更换默认到账卡======");

				map = (Map<String, Object>) KBpageRequest.KBQuickBindDebitCard(orderCode);

				String respCode = (String) map.get("resp_code");
				String respMessage = (String) map.get("resp_message");

				if ("000000".equals(respCode)) {
					if (kbBindCardByBankCard == null || !"1".equals(kbBindCardByBankCard.getStatus())) {
						LOG.info("用户需要绑卡======");

						return ResultWrap.init(CommonConstants.SUCCESS, "成功",
								ip + "/v1.0/paymentgateway/topup/tokbbindcard?bankName="
										+ URLEncoder.encode(creditCardBankName, "UTF-8") + "&cardType="
										+ URLEncoder.encode(creditCardCardType, "UTF-8") + "&bankCard="
										+ creditCardbankCard + "&orderCode=" + orderCode + "&expiredTime=" + expiredTime
										+ "&securityCode=" + securityCode + "&productCode=" + productCode
										+ "&ipAddress=" + ip);

					} else {

						LOG.info("用户直接发起交易======");

						String expiredTime1 = kbBindCardByBankCard.getExpiredTime();
						String securityCode1 = kbBindCardByBankCard.getSecurityCode();
						
						return ResultWrap.init(CommonConstants.SUCCESS, "成功",
								ip + "/v1.0/paymentgateway/topup/tokbquickpay?bankName="
										+ URLEncoder.encode(creditCardBankName, "UTF-8") + "&orderDesc="
										+ URLEncoder.encode(extra, "UTF-8") + "&bankCard="
										+ creditCardbankCard + "&orderCode=" + orderCode + "&amount=" + amount
										+ "&productCode=" + productCode + "&expiredTime=" + expiredTime1
										+ "&securityCode=" + securityCode1 + "&ipAddress=" + ip);

					}
				} else {

					return ResultWrap.init(CommonConstants.FALIED, respMessage);
				}
			} else {
				if (kbBindCardByBankCard == null || !"1".equals(kbBindCardByBankCard.getStatus())) {
					LOG.info("用户需要绑卡======");

					return ResultWrap.init(CommonConstants.SUCCESS, "成功",
							ip + "/v1.0/paymentgateway/topup/tokbbindcard?bankName="
									+ URLEncoder.encode(creditCardBankName, "UTF-8") + "&cardType="
									+ URLEncoder.encode(creditCardCardType, "UTF-8") + "&bankCard=" + creditCardbankCard
									+ "&orderCode=" + orderCode + "&expiredTime=" + expiredTime + "&securityCode="
									+ securityCode + "&productCode=" + productCode + "&ipAddress=" + ip);

				} else {

					LOG.info("用户直接发起交易======");

					String expiredTime1 = kbBindCardByBankCard.getExpiredTime();
					String securityCode1 = kbBindCardByBankCard.getSecurityCode();
					
					return ResultWrap.init(CommonConstants.SUCCESS, "成功",
							ip + "/v1.0/paymentgateway/topup/tokbquickpay?bankName="
									+ URLEncoder.encode(creditCardBankName, "UTF-8") + "&orderDesc="
									+ URLEncoder.encode(extra, "UTF-8") + "&bankCard=" + creditCardbankCard
									+ "&orderCode=" + orderCode + "&amount=" + amount + "&productCode=" + productCode
									+ "&expiredTime=" + expiredTime1 + "&securityCode=" + securityCode1 + "&ipAddress=" + ip);

				}

			}

		}

	}

}
