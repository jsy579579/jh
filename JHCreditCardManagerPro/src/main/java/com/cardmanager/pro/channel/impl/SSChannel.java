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
public class SSChannel extends ChannelRoot implements ChannelBaseAPI {

	private final static String GET_CHANNEL_URL_ACCOUNT_RUL = null;

	private final static String GET_ORDER_STATUS_URL = "/v1.0/paymentgateway/topup/ss/orderquery";

	private final static String IS_REGISTER_TO_CHANNEL_URL = "/v1.0/paymentgateway/topup/ss/torepayment";
	
	@Override
	public JSONObject getChannelUserAccount(LinkedMultiValueMap<String, String> requestEntity) {
		return JSONObject.fromObject(ResultWrap.init(CommonConstants.FALIED, "该通道无法查询通道帐户余额"));
	}

	@Override
	public JSONObject getOrderStatus(LinkedMultiValueMap<String, String> requestEntity, String orderType) {
		if (!requestEntity.containsKey("orderCode")) {
			return JSONObject.fromObject(ResultWrap.init(CommonConstants.FALIED, "缺少参数orderCode"));
		}
		if (!requestEntity.containsKey("bankCard")) {
			return JSONObject.fromObject(ResultWrap.init(CommonConstants.FALIED, "缺少参数bankCard"));
		}
		return this.postForJSON(new RestTemplate(), requestEntity, propertiesConfig.getPaymentgatewayIp()+GET_ORDER_STATUS_URL);
	}

	@Override
	public JSONObject isRegisterToChannel(LinkedMultiValueMap<String, String> requestEntity) {
		if (!requestEntity.containsKey("bankCard")) {
			return JSONObject.fromObject(ResultWrap.init(CommonConstants.FALIED, "缺少参数bankCard"));
		}
		if (!requestEntity.containsKey("idCard")) {
			return JSONObject.fromObject(ResultWrap.init(CommonConstants.FALIED, "缺少参数idCard"));
		}
		if (!requestEntity.containsKey("phone")) {
			return JSONObject.fromObject(ResultWrap.init(CommonConstants.FALIED, "缺少参数phone"));
		}
		if (!requestEntity.containsKey("userName")) {
			return JSONObject.fromObject(ResultWrap.init(CommonConstants.FALIED, "缺少参数userName"));
		}
		if (!requestEntity.containsKey("bankName")) {
			return JSONObject.fromObject(ResultWrap.init(CommonConstants.FALIED, "缺少参数bankName"));
		}
		return this.postForJSON(new RestTemplate(), requestEntity, propertiesConfig.getPaymentgatewayIp()+IS_REGISTER_TO_CHANNEL_URL);
	}

}
