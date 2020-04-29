package com.jh.paymentgateway.controller.yxe;

import com.jh.paymentgateway.basechannel.BaseChannel;
import com.jh.paymentgateway.business.TopupRequestBusiness;
import com.jh.paymentgateway.controller.yxe.service.YXEpageRequset;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class
YXEEntrance extends BaseChannel implements TopupRequestBusiness {

    private static final Logger LOG = LoggerFactory.getLogger(YXEEntrance.class);

    @Autowired
    YXEpageRequset yxEpageRequset;

    @Override
    public Map<String, Object> topupRequest(Map<String, Object> params) throws Exception {
        PaymentRequestParameter bean = (PaymentRequestParameter) params.get("paymentRequestParameter");
        Map<String, Object> map = new HashMap<>();
        String orderCode = bean.getOrderCode();//订单号
        String orderType = bean.getOrderType();//订单类型 10 消费  11 还款
        if ("10".equals(orderType)) {
            LOG.info("根据判断进入消费任务======"+orderCode);
            // 用户进入消费任务
            map = (Map<String, Object>) yxEpageRequset.toPay(orderCode);
        }
        if ("11".equals(orderType)) {
            LOG.info("根据判断进入还款任务======"+orderCode);
            // 用户进入还款任务
            map = (Map<String, Object>) yxEpageRequset.toSettle(orderCode);
        }
        return map;
    }
}
