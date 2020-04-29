package com.jh.paymentgateway.controller.hqm;

import com.jh.paymentgateway.basechannel.BaseChannel;
import com.jh.paymentgateway.business.TopupRequestBusiness;
import com.jh.paymentgateway.controller.hqm.dao.HQMBusiness;
import com.jh.paymentgateway.controller.hqm.pojo.HQMRegister;
import com.jh.paymentgateway.controller.hqm.service.HQMpageRequest;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class HQMEntrance extends BaseChannel implements TopupRequestBusiness {

    private static final Logger LOG = LoggerFactory.getLogger(HQMEntrance.class);

    @Autowired
    private HQMpageRequest hqMpageRequest;

    @Value("${payment.ipAddress}")
    private String ipAddress;

    @Autowired
    private HQMBusiness hqmBusiness;

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

        HQMRegister hqtRegister = hqmBusiness.findHQMRegisterByIdCard(idCard);

        if ("10".equals(orderType)) {
            LOG.info("根据判断进入消费任务======");
            // 判断用户是否修改费率或单笔手续费
            if (!rate.equals(hqtRegister.getRate()) | !extraFee.equals(hqtRegister.getExtraFee())) {
                maps = (Map<String, String>) hqMpageRequest.changeRate(idCard, rate, extraFee);
                if (!"000000".equals(maps.get("resp_code"))) {
                    return map;
                }
            }
            // 用户进入消费任务
            map = (Map<String, Object>) hqMpageRequest.topay(orderCode);
        }

        if ("11".equals(orderType)) {
            // 用户进入还款任务
            map = (Map<String, Object>) hqMpageRequest.transfer(orderCode);
        }

        return map ;
    }
}
