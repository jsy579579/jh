package com.jh.paymentchannel.service;

import java.util.ArrayList;
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
import com.jh.paymentchannel.pojo.WMYKNewChooseCity;
import com.jh.paymentchannel.pojo.WMYKXWKCityMerchant;
import com.jh.paymentchannel.util.Util;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;
import net.sf.json.JSONObject;

@Service
public class WMYKNewTopupPage extends BaseChannel implements TopupRequest {

	private static final Logger LOG = LoggerFactory.getLogger(WMYKNewTopupPage.class);

	@Autowired
	private TopupPayChannelBusiness topupPayChannelBusiness;

	@Autowired
	private Util util;

	@Autowired
	private WMYKNewpageRequest wmykNewpageRequest;

	@Value("${payment.ipAddress}")
	private String ipAddress;

	@Override
	public Map<String, String> topupRequest(Map<String, Object> params) throws Exception {
		PaymentOrder paymentOrder = (PaymentOrder) params.get("paymentOrder");
		HttpServletRequest request = (HttpServletRequest) params.get("request");
		String extra = (String) params.get("extra");
		String orderCode = paymentOrder.getOrdercode();
		String amount = paymentOrder.getAmount().toString();
		String orderType = paymentOrder.getType();
		String orderDesc = paymentOrder.getDesc();
		String bankCard = paymentOrder.getBankcard();
		String  userId = paymentOrder.getUserid() + "";
		
		Map<String, String> map = new HashMap<String, String>();

		String bankName = null;
		try {
			Map<String, Object> queryBankCardByCardNoAndUserId = this.queryBankCardByCardNoAndUserId(bankCard, "0", userId);
			Object object2 = queryBankCardByCardNoAndUserId.get("result");
			JSONObject fromObject = JSONObject.fromObject(object2);

			bankName = fromObject.getString("bankName");
		} catch (Exception e) {
			e.printStackTrace();
			
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "查询不到该银行卡信息,可能已被删除!");
			return map;
		}
		
		String storeNo = null;
		String merchantName = null;
		if(orderDesc.contains("M")) {
			
			if ("中国银行".equals(bankName) || bankName.contains("建设") || bankName.contains("交通") || bankName.contains("华夏")
					|| bankName.contains("兴业") || bankName.contains("中信") || bankName.contains("浦发")
					|| bankName.contains("浦东发展") || bankName.contains("广发") || bankName.contains("广东发展")
					|| bankName.contains("平安") || bankName.contains("邮储") || bankName.contains("邮政储蓄")
					|| bankName.contains("渣打") || bankName.contains("花旗") || bankName.contains("恒丰")
					|| bankName.contains("汇丰") || bankName.contains("工商")) {
				
				storeNo = orderDesc.substring(orderDesc.indexOf("M"), orderDesc.length()-1);
				
			}else {
				
				merchantName = orderDesc.substring(orderDesc.indexOf("|") + 1, orderDesc.indexOf("("));
				
				storeNo = orderDesc.substring(orderDesc.indexOf("M"), orderDesc.length()-1);
			
				WMYKXWKCityMerchant wmykxwkCityMerchantByMerchantCode = topupPayChannelBusiness.getWMYKXWKCityMerchantByMerchantCode(storeNo);
				
				if(wmykxwkCityMerchantByMerchantCode == null) {
					WMYKXWKCityMerchant wmykxwkCityMerchantByMerchantName = topupPayChannelBusiness.getWMYKXWKCityMerchantByMerchantName(merchantName);
					
					storeNo = wmykxwkCityMerchantByMerchantName.getMerchantCode();
				}
				
			}
			
		}else {
			WMYKNewChooseCity wmykNewChooseCity = topupPayChannelBusiness.getWMYKNewChooseCityByBankCard(bankCard);
			
			if(wmykNewChooseCity != null) {
				
				if ("中国银行".equals(bankName) || bankName.contains("建设") || bankName.contains("交通") || bankName.contains("华夏")
						|| bankName.contains("兴业") || bankName.contains("中信") || bankName.contains("浦发")
						|| bankName.contains("浦东发展") || bankName.contains("广发") || bankName.contains("广东发展")
						|| bankName.contains("平安") || bankName.contains("邮储") || bankName.contains("邮政储蓄")
						|| bankName.contains("渣打") || bankName.contains("花旗") || bankName.contains("恒丰")
						|| bankName.contains("汇丰") || bankName.contains("工商")) {
					
					List<String> wmykNewCity = topupPayChannelBusiness.getWMYKNewCityMerchantCodeByCityCode(wmykNewChooseCity.getCityCode().trim());
					
					List<String> list = new ArrayList<String>();
					
					Random random = new Random();
					
					for(String wcm : wmykNewCity) {
						
						list.add(wcm);
					}
					
					if(list != null && list.size()>0) {
						int j = random.nextInt(list.size());

						storeNo = list.get(j);
					
					}else {
						map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
						map.put(CommonConstants.RESP_MESSAGE, "没有门店信息!");
						return map;
					}
					
				}else {
					
					List<String> wmykNewCity = topupPayChannelBusiness.getWMYKXWKCityMerchantCodeByCityCode(wmykNewChooseCity.getCityCode().trim());
					
					List<String> list = new ArrayList<String>();
					
					Random random = new Random();
					
					for(String wcm : wmykNewCity) {
						
						list.add(wcm);
					}
					
					if(list != null && list.size()>0) {
						int j = random.nextInt(list.size());

						storeNo = list.get(j);
					
					}else {
						map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
						map.put(CommonConstants.RESP_MESSAGE, "没有门店信息!");
						return map;
					}
					
				}
				
				
				
			}else {
				
				if ("中国银行".equals(bankName) || bankName.contains("建设") || bankName.contains("交通") || bankName.contains("华夏")
						|| bankName.contains("兴业") || bankName.contains("中信") || bankName.contains("浦发")
						|| bankName.contains("浦东发展") || bankName.contains("广发") || bankName.contains("广东发展")
						|| bankName.contains("平安") || bankName.contains("邮储") || bankName.contains("邮政储蓄")
						|| bankName.contains("渣打") || bankName.contains("花旗") || bankName.contains("恒丰")
						|| bankName.contains("汇丰") || bankName.contains("工商")) {
					
					storeNo = "MD0059829";
					
				}else {
					
					storeNo = "MD0234358";
					
				}
				
			}
			
		}
		
		LOG.info("storeNo======"+storeNo);
		
		if ("10".equals(orderType)) {
			LOG.info("根据判断进入消费任务======");

			map = (Map<String, String>) wmykNewpageRequest.wmykNewPreOrder(orderCode, storeNo.trim());

		}

		if ("11".equals(orderType)) {

			map = (Map<String, String>) wmykNewpageRequest.wmykTransfer(orderCode, extra);

		}

		return map;

	}

}
