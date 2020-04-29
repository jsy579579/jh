package com.jh.paymentgateway.business.impl;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.jh.paymentgateway.basechannel.BaseChannel;
import com.jh.paymentgateway.business.TopupPayChannelBusiness;
import com.jh.paymentgateway.business.TopupRequestBusiness;
import com.jh.paymentgateway.controller.HQQuickpageRequest;
import com.jh.paymentgateway.pojo.HQBindCard;
import com.jh.paymentgateway.pojo.HQQuickRegister;
import com.jh.paymentgateway.pojo.HQRegister;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;
import com.jh.paymentgateway.util.Util;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;
import net.sf.json.JSONObject;

@Service
public class HQQuickTopupPage extends BaseChannel implements TopupRequestBusiness {

	private static final Logger LOG = LoggerFactory.getLogger(HQQuickTopupPage.class);

	@Autowired
	private Util util;

	@Value("${payment.ipAddress}")
	private String ip;

	@Autowired
	private TopupPayChannelBusiness topupPayChannelBusiness;

	@Autowired
	private HQQuickpageRequest hqQuickpageRequest;

	@Override
	public Map<String, Object> topupRequest(Map<String, Object> params) throws Exception {
		PaymentRequestParameter bean = (PaymentRequestParameter) params.get("paymentRequestParameter");

		String orderCode = bean.getOrderCode();
		String creditCardbankCard = bean.getBankCard();
		String debitCardNo = bean.getDebitCardNo();
		String amount = bean.getAmount();
		String expiredTime = bean.getExpiredTime();
		String securityCode = bean.getSecurityCode();
		String idCard = bean.getIdCard();
		String rate = bean.getRate();
		String extraFee = bean.getExtraFee();
		String creditCardBankName = bean.getCreditCardBankName();
		String creditCardCardType = bean.getCreditCardCardType();

		Map<String, Object> map = new HashMap<String, Object>();

		HQQuickRegister hqQuickRegister = topupPayChannelBusiness.getHQQuickRegisterByIdCard(idCard);

		if (hqQuickRegister == null) {
			LOG.info("用户需要进件======");

			map = (Map<String, Object>) hqQuickpageRequest.HQQuickRegister(orderCode);

			String respCode = (String) map.get("resp_code");
			String respMessage = (String) map.get("resp_message");
			
			if("000000".equals(respCode)) {
				LOG.info("进件成功,发起交易======");
				
				return ResultWrap.init(CommonConstants.SUCCESS, "成功",
						ip + "/v1.0/paymentgateway/topup/tohqquickpay?bankName="
								+ URLEncoder.encode(creditCardBankName, "UTF-8") + "&cardType="
								+ URLEncoder.encode(creditCardCardType, "UTF-8") + "&bankCard=" + creditCardbankCard
								+ "&ordercode=" + orderCode+ "&amount=" + amount + "&expiredTime=" + expiredTime + "&securityCode="
								+ securityCode + "&ipAddress=" + ip);
			}else {
				
				return ResultWrap.init(CommonConstants.FALIED, respMessage);
			}
			
		} else if (!debitCardNo.equals(hqQuickRegister.getBankCard()) || !rate.equals(hqQuickRegister.getRate())
				|| !extraFee.equals(hqQuickRegister.getExtraFee())) {
			LOG.info("用户需要修改进件信息======");

			map = (Map<String, Object>) hqQuickpageRequest.updateRegister(orderCode);
			
			String respCode = (String) map.get("resp_code");
			String respMessage = (String) map.get("resp_message");
			
			if("000000".equals(respCode)) {
				LOG.info("进件成功,发起交易======");
				
				return ResultWrap.init(CommonConstants.SUCCESS, "成功",
						ip + "/v1.0/paymentgateway/topup/tohqquickpay?bankName="
								+ URLEncoder.encode(creditCardBankName, "UTF-8") + "&cardType="
								+ URLEncoder.encode(creditCardCardType, "UTF-8") + "&bankCard=" + creditCardbankCard
								+ "&ordercode=" + orderCode+ "&amount=" + amount + "&expiredTime=" + expiredTime + "&securityCode="
								+ securityCode + "&ipAddress=" + ip);
			}else {
				
				return ResultWrap.init(CommonConstants.FALIED, respMessage);
			}
			
		} else {
			LOG.info("用户直接发起交易======");

			return ResultWrap.init(CommonConstants.SUCCESS, "成功",
					ip + "/v1.0/paymentgateway/topup/tohqquickpay?bankName="
							+ URLEncoder.encode(creditCardBankName, "UTF-8") + "&cardType="
							+ URLEncoder.encode(creditCardCardType, "UTF-8") + "&bankCard=" + creditCardbankCard
							+ "&ordercode=" + orderCode+ "&amount=" + amount + "&expiredTime=" + expiredTime + "&securityCode="
							+ securityCode + "&ipAddress=" + ip);

		}

		/*
		 * HQRegister hqRegister =
		 * topupPayChannelBusiness.getHQRegisterByIdCard(idCard); HQBindCard hqBindCard
		 * = topupPayChannelBusiness.getHQBindCardByBankCard(creditCardbankCard);
		 * 
		 * 
		 * if(hqRegister == null) { LOG.info("用户需要进件======");
		 * 
		 * return ResultWrap.init(CommonConstants.SUCCESS,"成功",ip +
		 * "/v1.0/paymentgateway/topup/tohqquickbankinfo?bankName=" +
		 * URLEncoder.encode(debitBankName, "UTF-8") + "&bankNo=" + debitCardNo +
		 * "&ordercode=" + orderCode + "&cardType=" +
		 * URLEncoder.encode(debitCardCardType, "UTF-8") + "&amount=" + amount +
		 * "&expiredTime=" + expiredTime+ "&securityCode=" + securityCode +
		 * "&ipAddress=" + ip + "&isRegister=0");
		 * 
		 * }
		 */

	}

}
