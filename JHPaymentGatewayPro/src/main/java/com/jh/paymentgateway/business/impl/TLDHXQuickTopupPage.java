package com.jh.paymentgateway.business.impl;

import cn.jh.common.utils.CommonConstants;
import com.jh.paymentgateway.basechannel.BaseChannel;
import com.jh.paymentgateway.business.TopupPayChannelBusiness;
import com.jh.paymentgateway.business.TopupRequestBusiness;
import com.jh.paymentgateway.controller.TLDHXQuickpageRequest;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;
import com.jh.paymentgateway.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @author keke
 * @date 2019/8/5
 * @description 通联（易生）小额代还
 */
@Service
public class TLDHXQuickTopupPage extends BaseChannel implements TopupRequestBusiness {

    @Autowired
    private TLDHXQuickpageRequest tldhxQuickpageRequest;

    private final Logger logger = LoggerFactory.getLogger(TLDHXQuickTopupPage.class);

    @Override
    public Map<String, Object> topupRequest(Map<String, Object> params) throws Exception {

        PaymentRequestParameter bean = (PaymentRequestParameter) params.get("paymentRequestParameter");

        Map<String, Object> maps = new HashMap<String, Object>();
        String orderCode = bean.getOrderCode();
        String orderType = bean.getOrderType();
        if ("10".equals(orderType)) {
            logger.info("判断进入通联消费任务==============");
            maps = (Map<String, Object>) tldhxQuickpageRequest.pay(orderCode);
        }else if ("11".equals(orderType)) {
            logger.info("判断进入通联还款任务==============");
            maps = (Map<String, Object>)tldhxQuickpageRequest.transferCreate(orderCode);
        } else {
            maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            maps.put(CommonConstants.RESP_MESSAGE, "匹配信用卡行别失败,请联系技术人员");;
        }
        return maps;
    }
}
