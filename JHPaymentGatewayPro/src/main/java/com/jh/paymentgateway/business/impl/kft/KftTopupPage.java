package com.jh.paymentgateway.business.impl.kft;

import com.jh.paymentgateway.business.TopupRequestBusiness;
import com.jh.paymentgateway.controller.kft.KFTPageRequest;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author zhangchaofeng
 * @date 2019/4/16
 * @description 快付通
 */

@Service
public class KftTopupPage implements TopupRequestBusiness {
    private static final Logger logger= LoggerFactory.getLogger(KftTopupPage.class);

    @Autowired
    KFTPageRequest kftPageRequest;


    @Override
    public Map<String, Object> topupRequest(Map<String, Object> params) throws Exception {

        PaymentRequestParameter bean = (PaymentRequestParameter) params.get("paymentRequestParameter");

        String orderCode = bean.getOrderCode();
        logger.info("订单号："+orderCode);

        Map<String, Object> map;

        logger.info("根据判断进入消费任务======");
        map = (Map<String, Object>) kftPageRequest.toPayPage(orderCode);

        return map;
    }
}

