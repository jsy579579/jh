package com.jh.paymentchannel.service;

import cn.jh.common.utils.CommonConstants;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradeAppPayModel;
import com.alipay.api.request.AlipayTradeAppPayRequest;
import com.alipay.api.response.AlipayTradeAppPayResponse;
import com.jh.paymentchannel.basechannel.BaseChannel;
import com.jh.paymentchannel.pojo.PaymentOrder;
import com.jh.paymentchannel.util.AlipayServiceEnvConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Service
public class ALIPAYAPPTopupPage extends BaseChannel implements TopupRequest{
    private static final Logger LOG = LoggerFactory.getLogger(ALIPAYAPPTopupPage.class);

    private static final Charset UTF_8 = StandardCharsets.UTF_8;

    @Value("${payment.ipAddress}")
    private String ipAddress;

    @Override
    public Map<String, String> topupRequest(Map<String, Object> params) throws Exception {
        PaymentOrder paymentOrder = (PaymentOrder) params.get("paymentOrder");
        HttpServletRequest request = (HttpServletRequest) params.get("request");

        String orderCode = paymentOrder.getOrdercode();
        String amount = paymentOrder.getAmount().toString();
        String outNotifyUrl = paymentOrder.getOutNotifyUrl();
        String desc = paymentOrder.getDesc();

        Map<String, String> maps = new HashMap<String, String>();

        LOG.info("APP_ID======="+AlipayServiceEnvConstants.APP_ID);
        LOG.info("private_key======="+AlipayServiceEnvConstants.PRIVATE_KEY);
        LOG.info("public_key======="+AlipayServiceEnvConstants.ALIPAY_PUBLIC_KEY);

        /*Map<String, String> myParams = OrderInfoUtil2_0.buildOrderParamMap(AlipayServiceEnvConstants.APP_ID, true, orderCode, amount, desc);
        String orderParam = OrderInfoUtil2_0.buildOrderParam(myParams);
        String sign = OrderInfoUtil2_0.getSign(myParams, AlipayServiceEnvConstants.PRIVATE_KEY, true);
        final String orderInfo = orderParam + "&" + sign;*/
        AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do",AlipayServiceEnvConstants.APP_ID, AlipayServiceEnvConstants.PRIVATE_KEY, "json", AlipayServiceEnvConstants.CHARSET, AlipayServiceEnvConstants.ALIPAY_PUBLIC_KEY, "RSA2");
        //实例化具体API对应的request类,类名称和接口名称对应,当前调用接口名称：alipay.trade.app.pay
        AlipayTradeAppPayRequest myRequest = new AlipayTradeAppPayRequest();
        //SDK已经封装掉了公共参数，这里只需要传入业务参数。以下方法为sdk的model入参方式(model和biz_content同时存在的情况下取biz_content)。
        AlipayTradeAppPayModel model = new AlipayTradeAppPayModel();
        model.setBody(desc);
        model.setSubject(desc);
        model.setOutTradeNo(orderCode);
        model.setTimeoutExpress("30m");
        model.setTotalAmount(amount);
        model.setProductCode("QUICK_MSECURITY_PAY");
        myRequest.setBizModel(model);
        myRequest.setNotifyUrl(ipAddress + "/v1.0/paymentchannel/topup/alipay/notify_call");
        myRequest.setReturnUrl(ipAddress + "/v1.0/paymentchannel/topup/sdjpaysuccess");
        AlipayTradeAppPayResponse response = null;
        try {
            //这里和普通的接口调用不同，使用的是sdkExecute
            response = alipayClient.sdkExecute(myRequest);
            LOG.info("支付宝支付响应信息============"+response.getBody());//就是orderString 可以直接给客户端请求，无需再做处理。
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        Map<String, String> map = new HashMap<>();
        map.put("orderInfo", response.getBody());
        Object json = JSONObject.toJSONString(map);
        maps.put(CommonConstants.RESP_CODE, "success");
        maps.put("channel_type", "jfapi");
        maps.put("redirect_url", json.toString());
        return maps;
    }

}
