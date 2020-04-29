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

/**
 * 
 * <p>Title: XSTopupPage</p>  
 * <p>Description: 新生支付通道入口类</p>  
 * @author Robin(WX/QQ:354476429)
 * @date 2018年10月16日
 */
@Service
public class XSKJTopupPag extends BaseChannel implements TopupRequestBusiness {

	private static final Logger LOG = LoggerFactory.getLogger(XSKJTopupPag.class);

	@Value("${payment.ipAddress}")
	private String ip;
	
	
	@Override
	public Map<String, Object> topupRequest(Map<String, Object> params) throws Exception {
		PaymentRequestParameter bean = (PaymentRequestParameter) params.get("paymentRequestParameter");
		Map<String, Object> map = new HashMap<String, Object>();
		map=ResultWrap.init(CommonConstants.SUCCESS, "请求成功",ip + "/v1.0/paymentgateway/topup/xskj/paypage?orderCode="
				+bean.getOrderCode() );
		
		LOG.info(bean.getOrderCode() + "=====请求支付返回=====" + map);
		return map;
	}

}
