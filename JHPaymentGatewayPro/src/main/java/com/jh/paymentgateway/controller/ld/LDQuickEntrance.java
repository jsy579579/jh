package com.jh.paymentgateway.controller.ld;

import com.jh.paymentgateway.basechannel.BaseChannel;
import com.jh.paymentgateway.business.TopupRequestBusiness;
import com.jh.paymentgateway.controller.ld.service.LDQuickPageRequest;
import com.jh.paymentgateway.pojo.JFBindCard;
import com.jh.paymentgateway.pojo.JFRegister;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 钱嘉雅酷大额（可自选商户）
 */
@Service
public class LDQuickEntrance extends BaseChannel implements TopupRequestBusiness {
	
	private static final Logger LOG= LoggerFactory.getLogger(LDQuickEntrance.class);

    @Autowired
    LDQuickPageRequest ldQuickPageRequest;

    @Override
    public Map<String, Object> topupRequest(Map<String, Object> params) throws Exception {

        PaymentRequestParameter bean = (PaymentRequestParameter) params.get("paymentRequestParameter");

        String orderCode = bean.getOrderCode();
        LOG.info("订单号："+orderCode);
        String bankCard = bean.getBankCard();
        String bankName = bean.getDebitBankName();
        String userName = bean.getUserName();
        String phone = bean.getCreditCardPhone();
        String ipAddress = bean.getIpAddress();
        String rate = bean.getRate();
        String extraFee = bean.getExtraFee();
        String idCard = bean.getIdCard();
        String orderType = bean.getOrderType();
        String userId = bean.getUserId();
        String cardtype = bean.getCreditCardCardType();
        String bankNo = bean.getDebitCardNo();
        String cardType = bean.getDebitCardCardType();
        String cardName = bean.getCreditCardBankName();
        String amount = bean.getAmount();
        String expiredTime = bean.getExpiredTime();
        String securityCode = bean.getSecurityCode();

        Map<String, Object> map = new HashMap<String, Object>();


        LOG.info("根据判断进入消费任务======");
        map = (Map<String, Object>) ldQuickPageRequest.Dockentrance(orderCode);

        return map;
    }
}
