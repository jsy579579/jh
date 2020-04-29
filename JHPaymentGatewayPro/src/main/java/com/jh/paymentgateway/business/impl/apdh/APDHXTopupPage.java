package com.jh.paymentgateway.business.impl.apdh;

import com.jh.paymentgateway.basechannel.BaseChannel;
import com.jh.paymentgateway.business.TopupRequestBusiness;
import com.jh.paymentgateway.controller.ap.APDHXpageRequest;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class APDHXTopupPage extends BaseChannel implements TopupRequestBusiness {

    private static final Logger LOG = LoggerFactory.getLogger(APDHXTopupPage.class);

    @Autowired
    private APDHXpageRequest apdhXpageRequest;
    @Override
    public Map<String, Object> topupRequest(Map<String, Object> params) throws Exception {
        PaymentRequestParameter bean = (PaymentRequestParameter) params.get("paymentRequestParameter");
        Map<String, Object> map = new HashMap<String, Object>();
        String orderCode = bean.getOrderCode();
        String orderType = bean.getOrderType();

        if ("10".equals(orderType)) {
            LOG.info("进入消费计划======================");
            map = (Map<String, Object>) apdhXpageRequest.pay(orderCode);

            return map;

        }

        if ("11".equals(orderType)) {
            LOG.info("进入还款计划==============================");
            map = (Map<String, Object>) apdhXpageRequest.transferCreate(orderCode);

            return map;
        }

        return map;
    }
}
