package com.cardmanager.pro.channel.impl;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;
import com.cardmanager.pro.channel.ChannelBaseAPI;
import com.cardmanager.pro.channel.ChannelRoot;
import net.sf.json.JSONObject;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

@Component
public class YXEChannel extends ChannelRoot implements ChannelBaseAPI {

    private final static String GET_CHANNEL_URL_ACCOUNT_RUL = "/v1.0/paymentgateway/repayment/yxe/balanceQuery";

    private final static String GET_CONSUME_ORDER_STATUS_URL = "/v1.0/paymentgateway/repayment/yxe/payQuery";

    private final static String GET_REPAYMENT_ORDER_STATUS_URL = "/v1.0/paymentgateway/repayment/yxe/reppayQuery";

    private final static String IS_REGISTER_TO_CHANNEL_URL = "/v1.0/paymentgateway/repayment/yxe/dockentrance";

    @Override
    public JSONObject getChannelUserAccount(LinkedMultiValueMap<String, String> requestEntity) {
        if (!requestEntity.containsKey("idCard")) {
            return JSONObject.fromObject(ResultWrap.init(CommonConstants.FALIED, "缺少参数idCard"));
        }
        return this.postForJSON(new RestTemplate(), requestEntity, propertiesConfig.getPaymentgatewayIp()+GET_CHANNEL_URL_ACCOUNT_RUL);
    }

    // 查询订单
    @Override
    public JSONObject getOrderStatus(LinkedMultiValueMap<String, String> requestEntity, String orderType) {
        if (!requestEntity.containsKey("orderCode")) {
            return JSONObject.fromObject(ResultWrap.init(CommonConstants.FALIED, "缺少参数orderCode"));
        }
        JSONObject resultJSON = null;
        if (CommonConstants.ORDER_TYPE_CONSUME.equals(orderType)) {
            resultJSON = this.postForJSON(new RestTemplate(), requestEntity, propertiesConfig.getPaymentgatewayIp()+GET_CONSUME_ORDER_STATUS_URL);
        }else if (CommonConstants.ORDER_TYPE_REPAYMENT.equals(orderType)) {
            resultJSON = this.postForJSON(new RestTemplate(), requestEntity, propertiesConfig.getPaymentgatewayIp()+GET_REPAYMENT_ORDER_STATUS_URL);
        }
        return resultJSON;
    }

    // 消费 or 待还
    @Override
    public JSONObject isRegisterToChannel(LinkedMultiValueMap<String, String> requestEntity) {
        return this.postForJSON(new RestTemplate(), requestEntity,
                propertiesConfig.getPaymentgatewayIp() + IS_REGISTER_TO_CHANNEL_URL);
    }
}
