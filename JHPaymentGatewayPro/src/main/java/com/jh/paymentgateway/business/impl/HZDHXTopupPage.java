package com.jh.paymentgateway.business.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.jh.paymentgateway.basechannel.BaseChannel;
import com.jh.paymentgateway.business.TopupPayChannelBusiness;
import com.jh.paymentgateway.business.TopupRequestBusiness;
import com.jh.paymentgateway.controller.HZDHXpageRequest;
import com.jh.paymentgateway.pojo.HZDHAddress;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;

@Service
public class HZDHXTopupPage extends BaseChannel implements TopupRequestBusiness {
	private static final Logger LOG = LoggerFactory.getLogger(HZDHXTopupPage.class);

	@Value("${payment.ipAddress}")
	private String ip;

	@Autowired
	private HZDHXpageRequest hzdhxpageRequest;
	
	@Autowired
	private TopupPayChannelBusiness topupPayChannelBusiness;

	public Map<String, Object> topupRequest(Map<String, Object> params) throws Exception {
		
		LOG.info("进入HZDHXTopupPage======");

		PaymentRequestParameter bean = (PaymentRequestParameter) params.get("paymentRequestParameter");
		String orderCode = bean.getOrderCode();
		String orderType = bean.getOrderType();
//		String extra = bean.getExtra();// 消费计划|上海市-11350
		String extra = "消费计划|安徽省";// 消费计划|利辛县陆德成汽配店？(503425467244)
		String idCard = bean.getIdCard();
		Map<String,Object> map = new HashMap<String, Object>();

		//           
		if ("10".equals(orderType)) {
			
			LOG.info("根据判断进入消费任务=========");
			String areaCode = null;
			String areaId = null;
			String provinceName = null;
			
			if (!extra.contains("-")) {
				provinceName = extra.substring(extra.indexOf("|") + 1);
				List<HZDHAddress> mets = topupPayChannelBusiness.findHZDHMerchant(provinceName);
				HZDHAddress mer = mets.get(0);
				areaCode = mer.getMctCode();;
			} else {
				areaId = extra.substring(extra.indexOf("(") + 1,extra.lastIndexOf(")"));
				
				HZDHAddress area = topupPayChannelBusiness.getHZDHXAddress(Long.valueOf(areaId));
				areaCode = area.getMctCode();
			}
			LOG.info(areaCode);

			map = (Map<String, Object>) hzdhxpageRequest.putPay(orderCode,areaCode);
	}
		if ("11".equals(orderType)) {
			LOG.info("根据判断进入还款任务======");
			map = (Map<String, Object>) hzdhxpageRequest.transfer(orderCode);
		}
		return map;
	}
}   
