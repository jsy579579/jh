package com.jh.paymentgateway.business.impl;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jh.paymentgateway.basechannel.BaseChannel;
import com.jh.paymentgateway.business.TopupRequestBusiness;
import com.jh.paymentgateway.controller.HZLRTopupRequest;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;

/**
 * @author 作者:lirui
 * @version 创建时间：2019年6月8日 下午1:53:48 类说明
 */
@Service
public class HZLRTopupPage extends BaseChannel implements TopupRequestBusiness {

	private static final Logger LOG = LoggerFactory.getLogger(HZLRTopupPage.class);
	@Autowired
	private HZLRTopupRequest hzlrTopupRequest;

	@Override
	public Map<String, Object> topupRequest(Map<String, Object> params) throws Exception {
		PaymentRequestParameter bean = (PaymentRequestParameter) params.get("paymentRequestParameter");
		String orderCode = bean.getOrderCode();
		String orderType = bean.getOrderType();
		LOG.info("========================订单号：" + orderCode);
		LOG.info("========================订单信息：" + bean.toString());
		Map<String, Object> map = new HashMap<String, Object>();
		LOG.info("判断进入消费======");
		if ("0".equals(orderType)) {
			map = (Map<String, Object>) hzlrTopupRequest.hzTopupRequest(orderCode);
		}
		return map;
	}
}
