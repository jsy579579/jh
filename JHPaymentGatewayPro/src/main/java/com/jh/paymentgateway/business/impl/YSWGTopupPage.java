package com.jh.paymentgateway.business.impl;


import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.jh.paymentgateway.basechannel.BaseChannel;
import com.jh.paymentgateway.business.TopupRequestBusiness;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;
import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;


@Service
public class YSWGTopupPage extends BaseChannel implements  TopupRequestBusiness{

	private static final Logger LOG = LoggerFactory.getLogger(YSWGTopupPage.class);
	
	
	@Value("${payment.ipAddress}")
	private String ip;

	@Override
	public Map<String, Object> topupRequest(Map<String, Object> params) throws Exception {
		PaymentRequestParameter bean = (PaymentRequestParameter) params.get("paymentRequestParameter");
		Map<String,Object> resultMap = new HashMap<String,Object>();
    	resultMap=ResultWrap.init(CommonConstants.SUCCESS,"成功",ip+"/v1.0/paymentgateway/topup/yswg/request?orderCode="+bean.getOrderCode());
        return resultMap;
	}
	
}

