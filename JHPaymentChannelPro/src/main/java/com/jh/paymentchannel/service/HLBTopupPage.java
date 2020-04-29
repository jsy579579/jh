package com.jh.paymentchannel.service;

import cn.jh.common.utils.CommonConstants;
import com.alibaba.fastjson.JSONObject;
import com.jh.paymentchannel.basechannel.BaseChannel;
import com.jh.paymentchannel.pojo.PaymentOrder;
import com.jh.paymentchannel.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * @title: HLBTopupPage
 * @projectName: juhepayment
 * @description: TODO
 * @author: huhao
 * @date: 2019/10/3 19:07
 */

@Service
public class HLBTopupPage extends BaseChannel implements TopupRequest {

    private static final Logger LOG = LoggerFactory.getLogger(ALIPAYAPPTopupPage.class);

    @Autowired
    private Util util;

    @Override
    public Map<String, String> topupRequest(Map<String, Object> params) throws Exception {
        PaymentOrder paymentOrder = (PaymentOrder) params.get("paymentOrder");
        HttpServletRequest request = (HttpServletRequest) params.get("request");
        String channelTag = String.valueOf(params.get("channelTag"));

        String orderCode = paymentOrder.getOrdercode();
        String amount = paymentOrder.getAmount().toString();
        String outNotifyUrl = paymentOrder.getOutNotifyUrl();
        String desc = paymentOrder.getDesc();

        Map<String, String> maps = new HashMap<String, String>();

        /** 请求参数*/
        LinkedMultiValueMap requestEntity = new LinkedMultiValueMap<String, String>();
        requestEntity.add("P2_orderId", orderCode);
        requestEntity.add("P5_orderAmount", amount);
        requestEntity.add("P12_goodsName", "会员");
        requestEntity.add("P13_goodsDetail", "会员");
        requestEntity.add("P14_desc", desc);
        requestEntity.add("channelTag",channelTag);
        String result = null;
        if (channelTag == null || "".equals(channelTag) || channelTag.trim().length() == 0){
            maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            maps.put(CommonConstants.RESP_MESSAGE, "通道类型为空！");
        }
        try {
            RestTemplate restTemplate = new RestTemplate();
            URI uri = util.getServiceUrl("paymentgateway", "error url request!");
            LOG.info(uri.toString());
            result = restTemplate.postForObject(uri.toString() + "/v1.0/paymentgateway/topup/hlb/createOrder",requestEntity,String.class);
            LOG.info("合利宝支付响应信息============" + result);
            JSONObject resultJSONObject = JSONObject.parseObject(result);
            if(!CommonConstants.SUCCESS.equals(resultJSONObject.getString(CommonConstants.RESP_CODE))){
                maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
                maps.put(CommonConstants.RESP_MESSAGE, "生成订单失败请重新发起");
            }
            maps.put(CommonConstants.RESP_CODE, "success");
            maps.put("channel_type", "hlb");
            maps.put(CommonConstants.RESP_MESSAGE, "生成订单成功等待扫码");
            LOG.info("二维码url：" + resultJSONObject.getString(CommonConstants.RESULT));
            maps.put(CommonConstants.RESULT, resultJSONObject.getString(CommonConstants.RESULT));
        } catch (RestClientException e) {
            maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            maps.put(CommonConstants.RESP_MESSAGE, "请求生成订单异常：" + e.getMessage());
        }
        return maps;
    }
}
