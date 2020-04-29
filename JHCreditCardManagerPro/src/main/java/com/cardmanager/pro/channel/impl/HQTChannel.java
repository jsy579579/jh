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
public class HQTChannel extends ChannelRoot implements ChannelBaseAPI {

    private final static String GET_CHANNEL_URL_ACCOUNT_RUL = "/v1.0/paymentgateway/topup/hqt/balanceQuery";

    private final static String GET_CONSUME_ORDER_STATUS_URL = "/v1.0/paymentgateway/topup/hqt/payQuery";

    private final static String IS_REGISTER_TO_CHANNEL_URL = "/v1.0/paymentgateway/topup/hqt/Dockentrance";

    @Override
    public JSONObject getChannelUserAccount(LinkedMultiValueMap<String, String> requestEntity) {
        if (!requestEntity.containsKey("idCard")) {
            return JSONObject.fromObject(ResultWrap.init(CommonConstants.FALIED, "缺少参数idCard"));
        }
        return this.postForJSON(new RestTemplate(), requestEntity,
                propertiesConfig.getPaymentgatewayIp() + GET_CHANNEL_URL_ACCOUNT_RUL);
    }

    @Override
    public JSONObject getOrderStatus(LinkedMultiValueMap<String, String> requestEntity, String orderType) {
        requestEntity.add("orderType", orderType);
        if (!requestEntity.containsKey("orderCode")) {
            return JSONObject.fromObject(ResultWrap.init(CommonConstants.FALIED, "缺少参数orderCode"));
        }
        String type = null;
        if (CommonConstants.ORDER_TYPE_CONSUME.equals(orderType)) {
            type = "133";
        } else if (CommonConstants.ORDER_TYPE_REPAYMENT.equals(orderType)) {
            type = "134";
        }
        requestEntity.add("transType", type);
        return this.postForJSON(new RestTemplate(), requestEntity,
                propertiesConfig.getPaymentgatewayIp() + GET_CONSUME_ORDER_STATUS_URL);
    }

    @Override
    public JSONObject isRegisterToChannel(LinkedMultiValueMap<String, String> requestEntity) {
        return this.postForJSON(new RestTemplate(), requestEntity,
                propertiesConfig.getPaymentgatewayIp() + IS_REGISTER_TO_CHANNEL_URL);
    }

}
