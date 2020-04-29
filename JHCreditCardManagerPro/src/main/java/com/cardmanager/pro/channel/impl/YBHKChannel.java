package com.cardmanager.pro.channel.impl;

import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;

import com.cardmanager.pro.channel.ChannelBaseAPI;
import com.cardmanager.pro.channel.ChannelRoot;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;
import net.sf.json.JSONObject;

@Component
public class YBHKChannel extends ChannelRoot implements ChannelBaseAPI {
	
	private final static String GET_CHANNEL_URL_ACCOUNT_RUL = "http://paymentchannel/v1.0/paymentchannel/topup/ybhk/balancequery";

	private final static String GET_CONSUME_ORDER_STATUS_URL = "http://paymentchannel/v1.0/paymentchannel/topup/ybhk/ordercodequery";
	
	private final static String GET_REPAYMENT_ORDER_STATUS_URL = "http://paymentchannel/v1.0/paymentchannel/topup/ybhk/transferquery";
	
	private final static String IS_REGISTER_TO_CHANNEL_URL = "http://paymentchannel/v1.0/paymentchannel/topup/ybhk/to/repayment";
	
	@Override
	public JSONObject getChannelUserAccount(LinkedMultiValueMap<String, String> requestEntity) {
		if (!requestEntity.containsKey("idCard")) {
			return JSONObject.fromObject(ResultWrap.init(CommonConstants.FALIED, "缺少参数idCard"));
		}
		if (!requestEntity.containsKey("balanceType")) {
			return JSONObject.fromObject(ResultWrap.init(CommonConstants.FALIED, "缺少参数balanceType"));
		}
		return this.postForJSON(restTemplate, requestEntity, GET_CHANNEL_URL_ACCOUNT_RUL);
	}

	@Override
	public JSONObject getOrderStatus(LinkedMultiValueMap<String, String> requestEntity, String orderType) {
		if (!requestEntity.containsKey("orderCode")) {
			return JSONObject.fromObject(ResultWrap.init(CommonConstants.FALIED, "缺少参数orderCode"));
		}
		JSONObject resultJSON = null;
		if (CommonConstants.ORDER_TYPE_CONSUME.equals(orderType)) {
			resultJSON = this.postForJSON(restTemplate, requestEntity, GET_CONSUME_ORDER_STATUS_URL);
		}else if (CommonConstants.ORDER_TYPE_REPAYMENT.equals(orderType)) {
			resultJSON = this.postForJSON(restTemplate, requestEntity, GET_REPAYMENT_ORDER_STATUS_URL);
		}
		return resultJSON;
	}

	@Override
	public JSONObject isRegisterToChannel(LinkedMultiValueMap<String, String> requestEntity) {
		if (!requestEntity.containsKey("bankCard")) {
			return JSONObject.fromObject(ResultWrap.init(CommonConstants.FALIED, "缺少参数bankCard"));
		}
		if (!requestEntity.containsKey("rate")) {
			return JSONObject.fromObject(ResultWrap.init(CommonConstants.FALIED, "缺少参数rate"));
		}
		if (!requestEntity.containsKey("extraFee")) {
			return JSONObject.fromObject(ResultWrap.init(CommonConstants.FALIED, "缺少参数extraFee"));
		}
		if (!requestEntity.containsKey("userId")) {
			return JSONObject.fromObject(ResultWrap.init(CommonConstants.FALIED, "缺少参数userId"));
		}
		return this.postForJSON(restTemplate, requestEntity, IS_REGISTER_TO_CHANNEL_URL);
	}

}
