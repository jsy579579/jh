package com.cardmanager.pro.channel.impl;

import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.cardmanager.pro.channel.ChannelBaseAPI;
import com.cardmanager.pro.channel.ChannelRoot;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;
import net.sf.json.JSONObject;

@Component
public class RYTChannel extends ChannelRoot implements ChannelBaseAPI {

	private final static String GET_CHANNEL_URL_ACCOUNT_RUL = "/v1.0/paymentgateway/topup/ryt/creditRHBalanceQuery";

	private final static String GET_CONSUME_ORDER_STATUS_URL = "/v1.0/paymentgateway/topup/ryt/consumeQuery";
	
	private final static String GET_REPAYMENT_ORDER_STATUS_URL = "/v1.0/paymentgateway/topup/ryt/withdrawQuery";
	
	private final static String IS_REGISTER_TO_CHANNEL_URL = "/v1.0/paymentgateway/topup/ryt/bindcard/p";
	
	@Override
	public JSONObject getChannelUserAccount(LinkedMultiValueMap<String, String> requestEntity) {
		if (!requestEntity.containsKey("idCard")) {
			return JSONObject.fromObject(ResultWrap.init(CommonConstants.FALIED, "缺少参数idCard"));
		}
		return this.postForJSON(new RestTemplate(), requestEntity, propertiesConfig.getPaymentgatewayIp()+GET_CHANNEL_URL_ACCOUNT_RUL);
	}

	@Override
	public JSONObject getOrderStatus(LinkedMultiValueMap<String, String> requestEntity, String orderType) {
		if (CommonConstants.ORDER_TYPE_CONSUME.equals(orderType)) {
			return this.postForJSON(new RestTemplate(), requestEntity, propertiesConfig.getPaymentgatewayIp()+GET_CONSUME_ORDER_STATUS_URL);
		}else if (CommonConstants.ORDER_TYPE_REPAYMENT.equals(orderType)) {
			return this.postForJSON(new RestTemplate(), requestEntity, propertiesConfig.getPaymentgatewayIp()+GET_REPAYMENT_ORDER_STATUS_URL);
		}else {
			throw new RuntimeException("orderType="+orderType+"=====订单类型有误!");
		}
	}

	@Override
	public JSONObject isRegisterToChannel(LinkedMultiValueMap<String, String> requestEntity) {
		String idcard = requestEntity.getFirst("idCard");
		String bankNo = requestEntity.getFirst("bankCard");
		requestEntity.add("idcard", idcard);
		requestEntity.add("bankNo", bankNo);
		requestEntity.add("order_code", bankNo);
		return this.postForJSON(new RestTemplate(), requestEntity, propertiesConfig.getPaymentgatewayIp()+IS_REGISTER_TO_CHANNEL_URL);
	}

}
