package com.jh.paymentgateway.business.impl;

import com.jh.paymentgateway.basechannel.BaseChannel;
import com.jh.paymentgateway.business.TopupPayChannelBusiness;
import com.jh.paymentgateway.business.TopupRequestBusiness;
import com.jh.paymentgateway.controller.xk.XKDHDPageRequest;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;
import com.jh.paymentgateway.pojo.xkdhd.XKDHDRegister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
@Service
public class XKDHDTopupPage  extends BaseChannel implements TopupRequestBusiness {


    private static final Logger LOG = LoggerFactory.getLogger(XKDHDTopupPage.class);

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private XKDHDPageRequest xkdhdPageRequest;

    @Value("${payment.ipAddress}")
    private String ipAddress;

    @Autowired
    private TopupPayChannelBusiness topupPayChannelBusiness;

    @Override
    public Map<String, Object> topupRequest(Map<String, Object> params) throws Exception {
        PaymentRequestParameter bean = (PaymentRequestParameter) params.get("paymentRequestParameter");
          Map<String,Object> map =new HashMap<>();
          Map<String,String> maps =new HashMap<>();

        String orderCode = bean.getOrderCode();
        String idCard = bean.getIdCard();
        String orderType = bean.getOrderType();
        String rate = bean.getRate();
        String extraFee = bean.getExtraFee();
        String bankCard = bean.getBankCard();
        LOG.info("xkdhd订单号====================="+orderCode);

     if("10".equals(orderType)){

         LOG.info("开始进入消费计划===============");

         map = (Map<String, Object>) xkdhdPageRequest.topay(orderCode);

     }
     if ("11".equals(orderType)){
         LOG.info("开始进入还款计划===============");
         map = (Map<String, Object>) xkdhdPageRequest.transfer(orderCode);


     }
        return map;
    }
}
