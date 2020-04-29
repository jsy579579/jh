package com.jh.paymentgateway.business.impl;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.jh.paymentgateway.basechannel.BaseChannel;
import com.jh.paymentgateway.business.TopupPayChannelBusiness;
import com.jh.paymentgateway.business.TopupRequestBusiness;
import com.jh.paymentgateway.controller.XSpageRequest;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;
import com.jh.paymentgateway.pojo.XSRegister;

import cn.jh.common.utils.CommonConstants;

/**
 * 
 * <p>Title: XSTopupPage</p>  
 * <p>Description: 新生支付通道入口类</p>  
 * @author Robin(WX/QQ:354476429)
 * @date 2018年10月16日
 */
@Service
public class XSTopupPage extends BaseChannel implements TopupRequestBusiness {

	private static final Logger LOG = LoggerFactory.getLogger(XSTopupPage.class);

	@Value("${payment.ipAddress}")
	private String ip;
	
	@Autowired
	private TopupPayChannelBusiness topupPayChannelBusiness;
	
	@Autowired
	private XSpageRequest xSpageRequest;
	
	@Override
	public Map<String, Object> topupRequest(Map<String, Object> params) throws Exception {
		PaymentRequestParameter bean = (PaymentRequestParameter) params.get("paymentRequestParameter");
		String idCard = bean.getIdCard();
		Map<String, Object> map = new HashMap<String, Object>();

		XSRegister xSRegister = null;
		if (xSRegister == null && CommonConstants.ORDER_TYPE_TOPUP.equals(bean.getOrderType())) {
			xSRegister = topupPayChannelBusiness.getXSRegisterByIdCard(idCard);
			map = xSpageRequest.xsRegister(bean);
			LOG.info(bean.getIdCard() + "=====注册返回=====" + map);
			Object respCode = map.get(CommonConstants.RESP_CODE);
			if (!CommonConstants.SUCCESS.equals(respCode)) {
				return map;
			}
			xSRegister = (XSRegister) map.get(CommonConstants.RESULT);
		}
		map = xSpageRequest.topupRequest(bean,xSRegister);
		LOG.info(bean.getOrderCode() + "=====请求支付返回=====" + map);
		return map;
	}

}
