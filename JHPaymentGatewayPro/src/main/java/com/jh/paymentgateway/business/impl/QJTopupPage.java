package com.jh.paymentgateway.business.impl;

import com.jh.paymentgateway.business.TopupRequestBusiness;
import com.jh.paymentgateway.controller.qj.QJPageRequest;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zy
 * @date 2019/6/12
 * @@description 钱嘉
 */
@Service
public class QJTopupPage implements TopupRequestBusiness {


    private static final Logger LOG= LoggerFactory.getLogger(QJTopupPage.class);

    @Autowired
    QJPageRequest qjPageRequest;

    @Override
    public Map<String, Object> topupRequest(Map<String, Object> params) throws Exception {

        PaymentRequestParameter bean = (PaymentRequestParameter) params.get("paymentRequestParameter");

        String orderCode = bean.getOrderCode();
        LOG.info("订单号："+orderCode);
        Map<String, Object> map = new HashMap();
        LOG.info("根据判断进入消费任务===============");

        map = (Map<String, Object>) qjPageRequest.getPay(orderCode);


        return map;
    }
}
