//package com.jh.paymentgateway.business.impl;
//
//import com.jh.paymentgateway.basechannel.BaseChannel;
//import com.jh.paymentgateway.business.TopupRequestBusiness;
//import com.jh.paymentgateway.controller.ypl.YPLpageRequest;
//import com.jh.paymentgateway.pojo.PaymentRequestParameter;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.util.HashMap;
//import java.util.Map;
//
//
///**
// * 易票联快捷
// */
//@Service
//public class YPLTopupPage  extends BaseChannel implements TopupRequestBusiness {
//
//    private static final Logger LOG = LoggerFactory.getLogger(YPLTopupPage.class);
//
//    @Autowired
//    YPLpageRequest yplpageRequest;
//
//
//    @Override
//    public Map<String, Object> topupRequest(Map<String, Object> params) throws Exception {
//
//        PaymentRequestParameter bean = (PaymentRequestParameter) params.get("paymentRequestParameter");
//
//        String orderCode = bean.getOrderCode();
//        LOG.info("订单号："+orderCode);
//
//        Map<String,Object> map = new HashMap<>();
//
//        LOG.info("开始进入易票联消费===========================");
//        map =  (Map<String, Object>)yplpageRequest.register(orderCode);
//
//        return map ;
//    }
//}
