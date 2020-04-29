package com.jh.paymentchannel.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.jh.paymentchannel.basechannel.BaseChannel;
import com.jh.paymentchannel.business.TopupPayChannelBusiness;
import com.jh.paymentchannel.pojo.PaymentOrder;
import com.jh.paymentchannel.pojo.WMYKChooseCity;
import com.jh.paymentchannel.pojo.WMYKCity;
import com.jh.paymentchannel.util.Util;

import cn.jh.common.utils.DateUtil;
import net.sf.json.JSONObject;

@Service
public class WMYKTopupPage extends BaseChannel implements TopupRequest {

	private static final Logger LOG = LoggerFactory.getLogger(WMYKTopupPage.class);

	@Autowired
	private TopupPayChannelBusiness topupPayChannelBusiness;

	@Autowired
	private Util util;

	@Autowired
	private WMYKpageRequest wmykpageRequest;

	@Value("${payment.ipAddress}")
	private String ipAddress;

	@Override
	public Map<String, String> topupRequest(Map<String, Object> params) throws Exception {
		PaymentOrder paymentOrder = (PaymentOrder) params.get("paymentOrder");
		HttpServletRequest request = (HttpServletRequest) params.get("request");
		String ordercode = paymentOrder.getOrdercode();
		String amount = paymentOrder.getAmount().toString();

		Map<String, String> map = new HashMap<String, String>();

		Map<String, Object> queryOrdercode = this.queryOrdercode(ordercode);
		Object object = queryOrdercode.get("result");
		JSONObject fromObject = JSONObject.fromObject(object);
		JSONObject resultObj = fromObject.getJSONObject("result");

		String bankCard = resultObj.getString("bankcard");
		String realAmount = resultObj.getString("realAmount");
		String orderType = resultObj.getString("type");

		WMYKChooseCity wmykChooseCity = topupPayChannelBusiness.getWMYKChooseCityByBankCard(bankCard);

		Random random = new Random();
		//String merchantNo = null;
		String cityCode = null;
		if (wmykChooseCity == null) {

			List<WMYKCity> wmykCity = topupPayChannelBusiness.getWMYKCity();
			List<String> list = new ArrayList<String>();
			
			for(WMYKCity wmyk : wmykCity) {
				
				list.add(wmyk.getCityCode());
				
			}
			
			int j = random.nextInt(list.size());

			cityCode = list.get(j);

			/*WMYKChooseCity city = new WMYKChooseCity();
			city.setBankCard(bankCard);
			city.setCityCode(code);
			city.setUpdateTime(DateUtil.getDateStringConvert(new String(), new Date(), "yyyy-MM-dd HH:mm:ss"));

			topupPayChannelBusiness.createWMYKChooseCity(city);

			WMYKChooseCity wmykChooseCity2 = topupPayChannelBusiness.getWMYKChooseCityByBankCard(bankCard);
			String cityCode = wmykChooseCity2.getCityCode();*/

			LOG.info("cityCode======"+cityCode);
			
			/*List<String> merchantCode = topupPayChannelBusiness.getWMYKCityMerchantCodeByCityCode(code.trim());

			LOG.info("merchantCode======"+merchantCode);
			
			int i = random.nextInt(merchantCode.size());

			LOG.info(merchantCode.get(i));

			merchantNo = merchantCode.get(i);*/

		} else {

			cityCode = wmykChooseCity.getCityCode();

			/*List<String> merchantCode = topupPayChannelBusiness.getWMYKCityMerchantCodeByCityCode(cityCode.trim());

			int i = random.nextInt(merchantCode.size());

			LOG.info(merchantCode.get(i));

			merchantNo = merchantCode.get(i);*/
		}

		if ("10".equals(orderType)) {
			LOG.info("根据判断进入消费任务======");

			map = (Map<String, String>) wmykpageRequest.wmykFastPay(request, ordercode, cityCode.trim());

		}

		if ("11".equals(orderType)) {

			map = (Map<String, String>) wmykpageRequest.wmykTransfer(request, ordercode);

		}

		return map;

	}

}
