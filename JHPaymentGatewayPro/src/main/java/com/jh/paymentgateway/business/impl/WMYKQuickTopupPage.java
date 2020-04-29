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
import com.jh.paymentgateway.business.TopupRequestBusiness;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;
import com.jh.paymentgateway.util.Util;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;
import net.sf.json.JSONObject;

@Service
public class WMYKQuickTopupPage extends BaseChannel implements  TopupRequestBusiness{

	private static final Logger LOG = LoggerFactory.getLogger(WMYKQuickTopupPage.class);

	@Autowired
	private Util util;
	
	@Value("${payment.ipAddress}")
	private String ip;
	
	@Override
	public Map<String, Object> topupRequest(Map<String, Object> params) throws Exception {
		PaymentRequestParameter bean = (PaymentRequestParameter) params.get("paymentRequestParameter");
		
		String orderCode = bean.getOrderCode();
		String creditCardbankCard = bean.getBankCard();
		String debitBankName = bean.getDebitBankName();
		String debitCardNo = bean.getDebitCardNo();
		String debitCardCardType = bean.getDebitCardCardType();
		String amount = bean.getAmount();
		String expiredTime = bean.getExpiredTime();
		String securityCode = bean.getSecurityCode();
		String ipAddress = bean.getIpAddress();
		
		Map<String,Object> map = new HashMap<String, Object>();
		
		RestTemplate restTemplate = new RestTemplate();
		MultiValueMap<String,String> multiValueMap = new LinkedMultiValueMap<String, String>();
		multiValueMap.add("bankCard", creditCardbankCard);
		
		String result = restTemplate.postForObject("http://106.15.47.73/v1.0/paymentchannel/topup/wmyknew/bindcardquery", multiValueMap, String.class);
		JSONObject jsonObject;
		String respCode;
		try {
			jsonObject = JSONObject.fromObject(result);
			respCode = jsonObject.getString("resp_code");
		} catch (Exception e) {
			LOG.error("查询绑卡信息出错======");
			map.put("resp_code", "failed");
			map.put("channel_type", "jf");
			map.put("resp_message", "交易排队中,请稍后重试!");
			return map;
		}
		
		if("999996".equals(respCode)) {
			LOG.info("用户需要绑卡======");
			return ResultWrap.init(CommonConstants.SUCCESS,"成功",ip + "/v1.0/paymentgateway/topup/towmykquickbankinfo?bankName="
					+ URLEncoder.encode(debitBankName, "UTF-8") + "&bankNo=" + debitCardNo + "&ordercode=" + orderCode
				    + "&cardType=" + URLEncoder.encode(debitCardCardType, "UTF-8") + "&amount=" + amount
					+ "&expiredTime=" + expiredTime+ "&securityCode=" + securityCode + "&ipAddress=" + ip + "&isRegister=0");
		}else {
			LOG.info("发起交易======");
			
			return ResultWrap.init(CommonConstants.SUCCESS,"成功",ip + "/v1.0/paymentgateway/topup/towmykquickbankinfo?bankName="
					+ URLEncoder.encode(debitBankName, "UTF-8") + "&bankNo=" + debitCardNo + "&ordercode=" + orderCode
				    + "&cardType=" + URLEncoder.encode(debitCardCardType, "UTF-8") + "&amount=" + amount
					+ "&expiredTime=" + expiredTime+ "&securityCode=" + securityCode + "&ipAddress=" + ip + "&isRegister=1");

		}
		
		
	}

}
