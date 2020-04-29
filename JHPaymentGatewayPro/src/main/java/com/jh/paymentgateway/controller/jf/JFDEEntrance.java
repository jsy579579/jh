package com.jh.paymentgateway.controller.jf;

import cn.jh.common.utils.CommonConstants;
import com.jh.paymentgateway.basechannel.BaseChannel;
import com.jh.paymentgateway.business.TopupPayChannelBusiness;
import com.jh.paymentgateway.business.TopupRequestBusiness;
import com.jh.paymentgateway.controller.jf.service.JFDHpageRequset;
import com.jh.paymentgateway.controller.qysh.YKHKEntrance;
import com.jh.paymentgateway.controller.tldh.dao.TLRegisterBusiness;
import com.jh.paymentgateway.controller.tldh.pojo.TLRegister;
import com.jh.paymentgateway.pojo.CJHKLRRegister;
import com.jh.paymentgateway.pojo.JFRegister;
import com.jh.paymentgateway.pojo.JFXRegister;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service

public class JFDEEntrance extends BaseChannel implements TopupRequestBusiness {

    private static final Logger logger = LoggerFactory.getLogger(JFDEEntrance.class);

    @Autowired
    JFDHpageRequset jfdHpageRequset;

    @Autowired
    TopupPayChannelBusiness topupPayChannelBusiness;

    @Override
    public Map<String, Object> topupRequest(Map<String, Object> params) throws Exception {
        PaymentRequestParameter bean = (PaymentRequestParameter) params.get("paymentRequestParameter");

        String orderCode = bean.getOrderCode();//订单号
        String orderType = bean.getOrderType();//订单类型 10 消费  11 还款
        String bankCard = bean.getBankCard();//银行卡 信用卡
        String rate = bean.getRate();//费率
        String extraFee = bean.getExtraFee();//额外手术费

        JFRegister jfR = topupPayChannelBusiness.getJFRegisterByIdCard(bean.getIdCard());

        Map<String, Object> map = new HashMap<String, Object>();

        if ("10".equals(orderType)) {
            logger.info("根据判断进入消费任务======" + orderCode);
            if (!extraFee.equals(jfR.getExtraFee()) | !bankCard.equals(jfR.getBankCard()) | !rate.equals(jfR.getRate())) {
                logger.info("即富大额代还===修改手续费===结算卡===去支付");
                Map maps = (Map) jfdHpageRequset.modifyCard(bankCard, bean.getPhone(), rate, extraFee, bean.getIdCard());
                if (!CommonConstants.SUCCESS.equals(maps.get(CommonConstants.RESP_CODE))) {
                    return maps;
                }
            }
            // 用户进入消费任务
            map = (Map<String, Object>) jfdHpageRequset.toPay(orderCode);
        }

        if ("11".equals(orderType)) {
            logger.info("根据判断进入还款任务======" + orderCode);
            // 用户进入还款任务
            map = (Map<String, Object>) jfdHpageRequset.toSettle(orderCode);
        }
        return map;
    }
}
