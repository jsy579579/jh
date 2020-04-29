package com.jh.paymentgateway.business.impl;

import com.jh.paymentgateway.basechannel.BaseChannel;
import com.jh.paymentgateway.business.TopupPayChannelBusiness;
import com.jh.paymentgateway.business.TopupRequestBusiness;
import com.jh.paymentgateway.controller.HQXpageRequest;
import com.jh.paymentgateway.pojo.HQXBindCard;
import com.jh.paymentgateway.pojo.HQXRegister;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class HQXTopupPage extends BaseChannel implements TopupRequestBusiness {

    private static final Logger LOG = LoggerFactory.getLogger(HQXTopupPage.class);

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private HQXpageRequest hqxPageRequest;

    @Value("${payment.ipAddress}")
    private String ipAddress;

    @Autowired
    private TopupPayChannelBusiness topupPayChannelBusiness;

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
        LOG.info("qhx订单号========================"+orderCode);

        HQXRegister hqRegister = topupPayChannelBusiness.getHQXRegisterByIdCard(idCard);

        if ("10".equals(orderType)) {
            LOG.info("根据判断进入消费任务======");
            // 判断用户是否修改费率或单笔手续费
            if (!rate.equals(hqRegister.getRate()) | !extraFee.equals(hqRegister.getExtraFee())) {
                maps = (Map<String, String>) hqxPageRequest.changeRate(idCard,bankCard, rate, extraFee);
                if (!"000000".equals(maps.get("resp_code"))) {
                    return map;
                }
            }
            // 用户进入消费任务
            map = (Map<String, Object>) hqxPageRequest.topay(orderCode);
        }

        if ("11".equals(orderType)) {
            // 用户进入还款任务
            map = (Map<String, Object>) hqxPageRequest.transfer(orderCode);
        }

        return map ;
    }
}
