package com.jh.paymentgateway.controller.xs;

import com.jh.paymentgateway.basechannel.BaseChannel;
import com.jh.paymentgateway.business.TopupRequestBusiness;
import com.jh.paymentgateway.controller.xs.service.XsPageReques;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class XSTopupMappin extends BaseChannel implements TopupRequestBusiness {
    private static final Logger LOG = LoggerFactory.getLogger(XSTopupMappin.class);
    @Autowired
    private XsPageReques xsPageReques;


    @Override
    public Map<String, Object> topupRequest(Map<String, Object> params) throws Exception {
        PaymentRequestParameter bean = (PaymentRequestParameter) params.get("paymentRequestParameter");
        Map<String, Object> map = new HashMap<>();
        Map<String, String> maps = new HashMap<>();
        String orderCode = bean.getOrderCode();//订单号
        String orderType = bean.getOrderType();//订单类型 10 消费  11 还款
        String idCard = bean.getIdCard(); //身份证
        String bankCard = bean.getBankCard();//银行卡 信用卡
        String rate = bean.getRate();//费率
        String extraFee = bean.getExtraFee();//额外手续费
        LOG.info("qhx订单号========================" + orderCode);
        if ("10".equals(orderType)) {
            // 用户进入消费任务
            map = (Map<String, Object>) xsPageReques.Advanceconsumetask(orderCode);
        }

        if ("11".equals(orderType)) {
            // 用户进入还款任务
            map = (Map<String, Object>) xsPageReques.repaymenttask(orderCode);
        }

        return map;
    }
}
