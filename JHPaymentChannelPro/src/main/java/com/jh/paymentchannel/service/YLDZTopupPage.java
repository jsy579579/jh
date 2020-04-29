package com.jh.paymentchannel.service;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.jh.paymentchannel.basechannel.BaseChannel;
import com.jh.paymentchannel.business.TopupPayChannelBusiness;
import com.jh.paymentchannel.pojo.PaymentOrder;
import com.jh.paymentchannel.pojo.YLDZBindCard;
import com.jh.paymentchannel.util.Util;

import net.sf.json.JSONObject;

@Service
public class YLDZTopupPage extends BaseChannel implements TopupRequest {

	private static final Logger LOG = LoggerFactory.getLogger(YLDZTopupPage.class);

	@Autowired
	private TopupPayChannelBusiness topupPayChannelBusiness;

	@Autowired
	private Util util;

	@Autowired
	private YLDZpageRequest yldzPageRequest;

	@Value("${payment.ipAddress}")
	private String ipAddress;

	@Override
	public Map<String, String> topupRequest(Map<String, Object> params) throws Exception {
		PaymentOrder paymentOrder = (PaymentOrder) params.get("paymentOrder");
		HttpServletRequest request = (HttpServletRequest) params.get("request");
		String ordercode = paymentOrder.getOrdercode();
		String amount = paymentOrder.getAmount().toString();

		Map<String, String> map = new HashMap<String, String>();

		Map<String, Object> queryOrdercode = this.queryOrdercode(ordercode);
		Object object = queryOrdercode.get("result");
		JSONObject fromObject = JSONObject.fromObject(object);
		JSONObject resultObj = fromObject.getJSONObject("result");

		String userId = resultObj.getString("userid");
		String bankCard = resultObj.getString("bankcard");
		String desc = resultObj.getString("desc");

		Map<String, Object> queryBankCardByCardNoAndUserId = this.queryBankCardByCardNoAndUserId(bankCard, "0", userId);

		Object object2 = queryBankCardByCardNoAndUserId.get("result");
		fromObject = JSONObject.fromObject(object2);

		String userName = fromObject.getString("userName");
		String idCard = fromObject.getString("idcard");
		String phone = fromObject.getString("phone");
		String bankName = fromObject.getString("bankName");
		String cardtype = fromObject.getString("cardType");
		String nature = fromObject.getString("nature");
		String securityCode = fromObject.getString("securityCode");
		String expiredTime = fromObject.getString("expiredTime");

		if (expiredTime == null || "".equals(expiredTime) || "null".equals(expiredTime)) {
			expiredTime = "";
		}

		if (securityCode == null || "".equals(securityCode) || "null".equals(securityCode)) {
			securityCode = "";
		}

		YLDZBindCard yldzBindCard = topupPayChannelBusiness.getYLDZBindCardByBankCard(bankCard);

		if (yldzBindCard == null || !"1".equals(yldzBindCard.getStatus())) {
			LOG.info("需要签约绑卡======");

			map.put("resp_code", "success");
			map.put("channel_type", "jf");
			map.put("redirect_url", ipAddress + "/v1.0/paymentchannel/topup/toyldzbindcard/page?bankName="
					+ URLEncoder.encode(bankName, "UTF-8") + "&bankCard=" + bankCard + "&amount=" + amount
					+ "&ordercode=" + ordercode + "&cardtype=" + URLEncoder.encode(cardtype, "UTF-8") + "&nature=" + URLEncoder.encode(nature, "UTF-8") + "&expiredTime="
					+ expiredTime + "&securityCode=" + securityCode + "&ipAddress=" + ipAddress);
			return map;

		} else {
			LOG.info("发起交易======");
			
			map.put("resp_code", "success");
			map.put("channel_type", "jf");
			map.put("redirect_url", ipAddress + "/v1.0/paymentchannel/topup/toyldzpay/page?amount=" + amount
					+ "&ordercode=" + ordercode + "&desc="
					+ URLEncoder.encode(desc, "UTF-8") + "&ipAddress=" + ipAddress);
			return map;
			
		}

	}

}
