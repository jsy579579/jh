package com.jh.paymentgateway.business.impl;

import java.math.BigDecimal;
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
import com.jh.paymentgateway.controller.LMDHpageRequest;
import com.jh.paymentgateway.pojo.LMDHRegister;
import com.jh.paymentgateway.pojo.LMTAddress;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;


@Service
public class LMDHTopupPage extends BaseChannel implements TopupRequestBusiness {
	private static final Logger LOG = LoggerFactory.getLogger(LMDHTopupPage.class);

	@Value("${payment.ipAddress}")
	private String ip;

	@Autowired
	private LMDHpageRequest lmdhpageRequest;
	
	@Autowired
	private TopupPayChannelBusiness topupPayChannelBusiness;

	public Map<String, Object> topupRequest(Map<String, Object> params) throws Exception {
		
		LOG.info("进入LMDHTopupPage======");

		PaymentRequestParameter bean = (PaymentRequestParameter) params.get("paymentRequestParameter");
		String orderCode = bean.getOrderCode();
		String orderType = bean.getOrderType();
		String extra = bean.getExtra();// 消费计划|福建省-泉州市-350500
//		String extra = "消费计划|北京市-市辖区-东城区";// 消费计划|山东省-青岛市
		String idCard = bean.getIdCard();
		Map<String,Object> map = new HashMap<String, Object>();

		//           
		if ("10".equals(orderType)) {
			
			LOG.info("根据判断进入消费任务=========");
			String provinceOfBank = null;
			String cityOfBank = null;
			String areaOfBank = null;
			String provinceCode = null;
			String cityCode = null;
			String areaCode = null;
			
			if (!extra.contains("-")) {
				provinceOfBank = "上海市";
				provinceCode = "31";
				cityOfBank = "市辖区";
				cityCode = "3101";
				areaOfBank = "浦东新区";
				areaCode = "310115";
			} else {
				provinceOfBank = extra.substring(extra.indexOf("|") + 1, extra.indexOf("-"));
				cityOfBank = extra.substring(extra.indexOf("-") + 1, extra.lastIndexOf("-"));
				areaOfBank = extra.substring(extra.lastIndexOf("-") + 1 );
				LMTAddress province = topupPayChannelBusiness.getLMTProvinceCode(provinceOfBank);
				provinceCode = province.getCode();
				long proCode = province.getId();
				LMTAddress city = topupPayChannelBusiness.getLMTProvinceCode(cityOfBank,String.valueOf(proCode));
				cityCode = city.getCode();
				long ciCode = city.getId();
				LMTAddress area = topupPayChannelBusiness.getLMTProvinceCode(areaOfBank,String.valueOf(ciCode));
				areaCode = area.getCode();
			}
			LOG.info(provinceOfBank +":"+ provinceCode + "," + cityOfBank + ":" + cityCode + "," + areaOfBank + ":" + areaCode);

			map = (Map<String, Object>) lmdhpageRequest.putPay(orderCode,provinceOfBank,cityOfBank,areaOfBank,provinceCode,cityCode,areaCode);
	}
		if ("11".equals(orderType)) {
			LOG.info("根据判断进入还款任务======");
			map = (Map<String, Object>) lmdhpageRequest.transfer(orderCode);
		}
		return map;
	}
}   
