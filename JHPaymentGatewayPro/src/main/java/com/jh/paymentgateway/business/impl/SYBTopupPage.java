package com.jh.paymentgateway.business.impl;

import com.jh.paymentgateway.business.TopupPayChannelBusiness;
import com.jh.paymentgateway.business.TopupRequestBusiness;
import com.jh.paymentgateway.config.RedisUtil;
import com.jh.paymentgateway.controller.SYBpageRequest;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;
import com.jh.paymentgateway.pojo.SYBRegister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * @description 收银宝
 */
@Service
public class
SYBTopupPage implements TopupRequestBusiness {
private static final Logger logger= LoggerFactory.getLogger(SYBTopupPage.class);

        @Autowired
        SYBpageRequest syBpageRequest;
        @Autowired
        private RedisUtil redisUtil;
        @Autowired
        private TopupPayChannelBusiness topupPayChannelBusiness;
@Override
public Map<String, Object> topupRequest(Map<String, Object> params) throws Exception {

        PaymentRequestParameter bean = (PaymentRequestParameter) params.get("paymentRequestParameter");

        String orderCode = bean.getOrderCode();
        String orderType = bean.getOrderType();
        String extra = bean.getExtra();// 消费计划|福建省-泉州市-350500
        logger.info("订单号："+orderCode);

        Map<String, Object> maps = new HashMap<String, Object>();
        PaymentRequestParameter prp = redisUtil.getPaymentRequestParameter(orderCode);
        String idCard = prp.getIdCard();
        SYBRegister syb = topupPayChannelBusiness.findSYBRegisterbyIdcard(idCard);
        String rate = prp.getRate();
        String extraFee = prp.getExtraFee();
//
//        logger.info("根据判断进入消费任务======");
//        map = (Map<String, Object>) syBpageRequest.getRegister(orderCode);


        if ("10".equals(orderType)) {
                if (!syb.getRate().equals(rate) || !syb.getExtraFee().equals(extraFee)) {
                        logger.info("=====================================费率不匹配，开始修改费率=======================================");
                        logger.info("===========rate=="+rate+"========extraFee==="+extraFee+"=========idCard====="+idCard+"=========================");

                        maps = (Map<String, Object>) syBpageRequest.changerate(rate, extraFee,idCard);
                        if (!"000000".equals(maps.get("resp_code"))) {
                                return maps;
                        }
                }
                logger.info("==========================判断进入消费任务============================");
                String mccid = extra.substring(extra.lastIndexOf("-")+1, extra.length());  //截取mccid
                String sub = extra.substring(0, extra.lastIndexOf("-"));
                String city = sub.substring(sub.lastIndexOf("-")+1, sub.length());  //截取city
                logger.info("落地的商户类型======================mccid================"+   mccid);
                logger.info("落地的城市======================city==================" +   city);
                maps = (Map<String, Object>) syBpageRequest.payone(orderCode, mccid,city);
        }

        if ("11".equals(orderType)) {
                logger.info("==========================判断进入还款任务============================");
                maps = (Map<String, Object>) syBpageRequest.cash(orderCode);
        }

        return maps;
        }
}
