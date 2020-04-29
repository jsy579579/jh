package com.jh.paymentgateway.business.impl;

import com.jh.paymentgateway.basechannel.BaseChannel;
import com.jh.paymentgateway.business.NewTopupPayChannelBusiness;

import com.jh.paymentgateway.business.TopupRequestBusiness;
import com.jh.paymentgateway.controller.HXDHDTopupRequest;

import com.jh.paymentgateway.pojo.PaymentRequestParameter;

import com.jh.paymentgateway.pojo.hxdhd.HXDHDBindCard;
import com.jh.paymentgateway.pojo.hxdhd.HXDHDRegister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @title: HXDHDTopupPage
 * @projectName: juhepay
 * @description: TODO
 * @author: huhao
 * @date: 2019/8/16 19:37
 */

@Component
public class HXDHDTopupPage extends BaseChannel implements TopupRequestBusiness {
    @Value("${payment.ipAddress}")
    private String ipAddress;

    @Autowired
    private NewTopupPayChannelBusiness newTopupPayChannelBusiness;

    @Autowired
    private HXDHDTopupRequest hxdhdTopupRequest;

    private static final Logger LOG = LoggerFactory.getLogger(HXDHDTopupPage.class);

    @Override
    public Map<String, Object> topupRequest(Map<String, Object> params) throws Exception {
        PaymentRequestParameter bean = (PaymentRequestParameter) params.get("paymentRequestParameter");
        Map<String, Object> map = new HashMap<>();
        String orderCode = bean.getOrderCode();
        String orderType = bean.getOrderType();
        String bankName1 = bean.getCreditCardBankName();
        String rate = bean.getRate();
        String idCard = bean.getIdCard();
        String extraFee = bean.getExtraFee();
        String bankCard = bean.getBankCard();
        String extra = bean.getExtra();// 消费计划|福建省-泉州市-350500
        HXDHDRegister register = newTopupPayChannelBusiness.getHXDHDRegisterByBankCard(bankCard);
        HXDHDBindCard bindCard = newTopupPayChannelBusiness.getHXDHDBindCardByBankCard(bankCard);
        if ("10".equals(orderType)) {
            LOG.info("进入消费计划======================");
            map = (Map<String, Object>) hxdhdTopupRequest.pay(orderCode);
            return map;
        }
        if ("11".equals(orderType)) {
            LOG.info("进入还款计划==============================");
            map = (Map<String, Object>) hxdhdTopupRequest.transferCreate(orderCode);
            return map;
        }
        return map;
    }
}

