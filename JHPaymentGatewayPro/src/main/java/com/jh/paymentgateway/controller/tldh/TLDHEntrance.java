package com.jh.paymentgateway.controller.tldh;

import com.jh.paymentgateway.basechannel.BaseChannel;
import com.jh.paymentgateway.business.TopupRequestBusiness;
import com.jh.paymentgateway.controller.tldh.service.TLpageRequest;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
@Service
public class TLDHEntrance extends BaseChannel implements TopupRequestBusiness {

    private static final Logger logger= LoggerFactory.getLogger(TLDHEntrance.class);

    @Autowired
    TLpageRequest tLpageRequest;

    @Override
    public Map<String, Object> topupRequest(Map<String, Object> params) throws Exception {

        PaymentRequestParameter bean = (PaymentRequestParameter) params.get("paymentRequestParameter");

        String orderCode = bean.getOrderCode();//订单号
        String orderType = bean.getOrderType();//订单类型 10 消费  11 还款
        String bankCard = bean.getBankCard();//银行卡 信用卡
        String rate = bean.getRate();//费率
        String extraFee = bean.getExtraFee();//额外手术费

        Map<String, Object> map = new HashMap<String, Object>();

        if ("10".equals(orderType)) {
            logger.info("根据判断进入消费任务======"+orderCode);
            // 用户进入消费任务
            map = (Map<String, Object>) tLpageRequest.toPay(orderCode);
        }

        if ("11".equals(orderType)) {
            logger.info("根据判断进入还款任务======"+orderCode);
            // 用户进入还款任务
            map = (Map<String, Object>) tLpageRequest.toSettle(orderCode);
        }
        return map;

    }
}
