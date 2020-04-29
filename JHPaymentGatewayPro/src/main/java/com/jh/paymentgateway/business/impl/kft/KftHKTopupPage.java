package com.jh.paymentgateway.business.impl.kft;

import cn.jh.common.utils.CommonConstants;
import com.jh.paymentgateway.basechannel.BaseChannel;
import com.jh.paymentgateway.business.TopupPayChannelBusiness;
import com.jh.paymentgateway.business.TopupRequestBusiness;
import com.jh.paymentgateway.controller.CJHKXpageRequest;
import com.jh.paymentgateway.controller.kft.KFTDHPageRequest;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;
import com.jh.paymentgateway.util.Util;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zhangchaofeng
 * @date 2019/5/23
 * @description 快付通还款
 */
@Service
public class KftHKTopupPage extends BaseChannel implements TopupRequestBusiness {

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private KFTDHPageRequest kftdhPageRequest;

    @Value("${payment.ipAddress}")
    private String ipAddress;

    @Autowired
    private TopupPayChannelBusiness topupPayChannelBusiness;

    private final Logger logger = LoggerFactory.getLogger(KftHKTopupPage.class);

    @Override
    public Map<String, Object> topupRequest(Map<String, Object> params) throws Exception {

        PaymentRequestParameter bean = (PaymentRequestParameter) params.get("paymentRequestParameter");

        Map<String, Object> maps = new HashMap<String, Object>();
        String orderCode = bean.getOrderCode();
        String orderType = bean.getOrderType();
        String bankName1 = bean.getCreditCardBankName();
        String rate = bean.getRate();
        String idCard = bean.getIdCard();
        String extraFee = bean.getExtraFee();
        String extra = bean.getExtra();// 消费计划|福建省-泉州市-350500
        String bankName = Util.queryBankNameByBranchName(bankName1);


        List<String> kftBankCodeByName = topupPayChannelBusiness.findKftBankCodeByName(bankName);

        String[] arr= {};
        if(StringUtils.isNotBlank(extra)) {
            arr = extra.split("-");
        }

        if (kftBankCodeByName != null && kftBankCodeByName.size()!=0) {
            if ("10".equals(orderType)) {
                logger.info("判断进入消费任务==============");
                String cityName = "";
                String provinceName = null;
                if (!extra.contains("-")) {
                    cityName = "宝山区";
                    provinceName = "上海市";
                } else if(arr.length>=3) {
                    provinceName = extra.substring(extra.indexOf("|") + 1, extra.indexOf("-"));
                    cityName = extra.substring(extra.indexOf("-") + 1, extra.lastIndexOf("-"));
                } else {
                    provinceName = extra.substring(extra.indexOf("|") + 1, extra.indexOf("-"));
                    cityName = extra.substring(extra.indexOf("-") + 1, extra.length());
                }
                if(StringUtils.equals(cityName,"上海市")){
                    cityName = "宝山区";
                }else if(StringUtils.equals(cityName,"重庆市")){
                    cityName = "万州区";
                }else if(StringUtils.equals(cityName,"北京市")){
                    cityName = "东城区";
                }else if (StringUtils.equals(cityName,"天津市")){
                    cityName = "南开区";
                }

                maps = kftdhPageRequest.excuteTreatyCollect(orderCode, cityName);

            }

            if ("11".equals(orderType)) {
                logger.info("快付通根据判断进入还款任务======");
                maps = kftdhPageRequest.excutePayByRule(orderCode,"2");

            }

            return maps;
        } else {
            maps.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
            maps.put(CommonConstants.RESP_MESSAGE, "匹配信用卡行别失败,请联系技术人员");
            return maps;
        }

    }



}
