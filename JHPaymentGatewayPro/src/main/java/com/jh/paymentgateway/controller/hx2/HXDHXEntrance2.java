package com.jh.paymentgateway.controller.hx2;

import com.jh.paymentgateway.basechannel.BaseChannel;
import com.jh.paymentgateway.business.TopupPayChannelBusiness;
import com.jh.paymentgateway.business.TopupRequestBusiness;
import com.jh.paymentgateway.controller.HXDHXTopupRequest;
//import com.jh.paymentgateway.controller.hx2.service.HXDHXTopupRequest2;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;
import com.jh.paymentgateway.pojo.hx.HXDHXBindCard;
import com.jh.paymentgateway.pojo.hx.HXDHXRegister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.Map;

@Controller
public class HXDHXEntrance2 extends BaseChannel implements TopupRequestBusiness {

    @Value("${payment.ipAddress}")
    private String ipAddress;

    @Autowired
    private TopupPayChannelBusiness topupPayChannelBusiness;

    @Autowired
    private HXDHXTopupRequest2 hxdhxTopupRequest2;


    private static final Logger LOG = LoggerFactory.getLogger(HXDHXEntrance2.class);

    @Override
    public Map<String, Object> topupRequest(Map<String, Object> params) throws Exception {
        PaymentRequestParameter bean = (PaymentRequestParameter) params.get("paymentRequestParameter");


        Map<String, Object> map = new HashMap<String, Object>();
        String orderCode = bean.getOrderCode();
        String orderType = bean.getOrderType();
        String bankName1 = bean.getCreditCardBankName();
        String rate = bean.getRate();
        String idCard = bean.getIdCard();
        String extraFee = bean.getExtraFee();
        String bankCard = bean.getBankCard();
        String extra = bean.getExtra();// 消费计划|福建省-泉州市-350500
        HXDHXRegister register = topupPayChannelBusiness.getHXDHXRegisterByIdCard(idCard);
        HXDHXBindCard bindCard = topupPayChannelBusiness.getHXDHXBindCardByBankCard(bankCard);
        if ("10".equals(orderType)) {
            LOG.info("进入消费计划======================");
           map = (Map<String, Object>) hxdhxTopupRequest2.pay(orderCode);

            return map;

        }

        if ("11".equals(orderType)) {
            LOG.info("进入还款计划==============================");
             map = (Map<String, Object>) hxdhxTopupRequest2.transferCreate(orderCode);

            return map;
        }

        return map;
    }
}
