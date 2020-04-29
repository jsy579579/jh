package com.jh.paymentgateway.business.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.jh.paymentgateway.basechannel.BaseChannel;
import com.jh.paymentgateway.business.TopupPayChannelBusiness;
import com.jh.paymentgateway.business.TopupRequestBusiness;
import com.jh.paymentgateway.controller.GHTDHpageRequest;
import com.jh.paymentgateway.pojo.GHTCityMerchant;
import com.jh.paymentgateway.pojo.GHTXwkCityMerchant;
import com.jh.paymentgateway.pojo.PaymentRequestParameter;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;

@Service
public class GHTDHTopupPage extends BaseChannel implements  TopupRequestBusiness{

	private static final Logger LOG = LoggerFactory.getLogger(GHTDHTopupPage.class);

	@Value("${payment.ipAddress}")
	private String ip;
	
	@Autowired
	private TopupPayChannelBusiness topupPayChannelBusiness;
	
	@Autowired
	private GHTDHpageRequest ghtdhpageRequst;
	
	@Override
	public Map<String, Object> topupRequest(Map<String, Object> params) throws Exception {
		PaymentRequestParameter bean = (PaymentRequestParameter) params.get("paymentRequestParameter");
		
		String orderCode = bean.getOrderCode();
		String orderType = bean.getOrderType();
		String expiredTime = bean.getExpiredTime();
		String securityCode = bean.getSecurityCode();
		String extra = bean.getExtra();
		String orderDesc = bean.getExtra();
		String bankName = bean.getCreditCardBankName();
		
		Random random = new Random();
		List<String> list = new ArrayList<String>();
		Map<String,Object> map = new HashMap<String, Object>();
		String storeNo = null;
		if(orderDesc.contains("M")) {
			
			storeNo = orderDesc.substring(orderDesc.indexOf("M"), orderDesc.length()-1);
			
			if(bankName.contains("交通")) {
				GHTXwkCityMerchant ghtXwkCityMerchantByMerchantCode = topupPayChannelBusiness.getGHTXwkCityMerchantByMerchantCode(storeNo);
				if(ghtXwkCityMerchantByMerchantCode == null) {
					GHTCityMerchant ghtCityMerchantByMerchantCode = topupPayChannelBusiness.getGHTCityMerchantByMerchantCode(storeNo);
					if(ghtCityMerchantByMerchantCode != null) {
						String merchantName = ghtCityMerchantByMerchantCode.getMerchantName();
						GHTXwkCityMerchant ghtXwkCityMerchantByMerchantName = topupPayChannelBusiness.getGHTXwkCityMerchantByMerchantName(merchantName);
						if(ghtXwkCityMerchantByMerchantName != null) {
							storeNo = ghtXwkCityMerchantByMerchantName.getMerchantCode();
						}else {
							list.add("MD0663583");
							list.add("MD0663595");
							list.add("MD0669803");
							list.add("MD0669761");
							
							storeNo = list.get(random.nextInt(list.size()));
						}
					}else {
						list.add("MD0663583");
						list.add("MD0663595");
						list.add("MD0669803");
						list.add("MD0669761");
						
						storeNo = list.get(random.nextInt(list.size()));
					}
				}
			}
			
			if(bankName.contains("农业")) {
				GHTCityMerchant ghtXwkCityMerchantByMerchantCode = topupPayChannelBusiness.getGHTCityMerchantByMerchantCode(storeNo);
				if(ghtXwkCityMerchantByMerchantCode == null) {
					GHTXwkCityMerchant ghtCityMerchantByMerchantCode = topupPayChannelBusiness.getGHTXwkCityMerchantByMerchantCode(storeNo);
					if(ghtCityMerchantByMerchantCode != null) {
						String merchantName = ghtCityMerchantByMerchantCode.getMerchantName();
						GHTCityMerchant ghtXwkCityMerchantByMerchantName = topupPayChannelBusiness.getGHTCityMerchantByMerchantName(merchantName);
						if(ghtXwkCityMerchantByMerchantName != null) {
							storeNo = ghtXwkCityMerchantByMerchantName.getMerchantCode();
						}else {
							list.add("MD0638802"); //上海银杉小吃屋
							list.add("MD0638814"); //上海伍吃美食店
							list.add("MD0645022"); //上海弘一花店
							list.add("MD0644980"); //上海市徐汇区家清百货商店
							
							storeNo = list.get(random.nextInt(list.size()));
						}
					}else {
						list.add("MD0638802"); //上海银杉小吃屋
						list.add("MD0638814"); //上海伍吃美食店
						list.add("MD0645022"); //上海弘一花店
						list.add("MD0644980"); //上海市徐汇区家清百货商店
						
						storeNo = list.get(random.nextInt(list.size()));
					}
				}
			}
			
		}else {
			
			if(orderDesc.contains("-")) {
				String province = orderDesc.substring(orderDesc.indexOf("|") + 1, orderDesc.indexOf("-"));
				String city = orderDesc.substring(orderDesc.indexOf("-") + 1);
			
				if ("中国银行".equals(bankName) || bankName.contains("建设") || bankName.contains("华夏")
						|| bankName.contains("兴业") || bankName.contains("中信") || bankName.contains("浦发")
						|| bankName.contains("浦东发展") || bankName.contains("广发") || bankName.contains("广东发展")
						|| bankName.contains("平安") || bankName.contains("邮储") || bankName.contains("邮政储蓄")
						|| bankName.contains("渣打") || bankName.contains("花旗") || bankName.contains("恒丰")
						|| bankName.contains("汇丰") || bankName.contains("工商") || bankName.contains("农业")) {
					LOG.info("全渠道的商户门店======");
					
					List<String> ghtCityMerchantCodeByProvinceAndCity = topupPayChannelBusiness.getGHTCityMerchantCodeByProvinceAndCity(province.trim(), city.trim());
					
					for(String ghtmc : ghtCityMerchantCodeByProvinceAndCity) {
						
						list.add(ghtmc);
					}
					
					if(list != null && list.size()>0) {
						int j = random.nextInt(list.size());

						storeNo = list.get(j);
					
					}else {
						
						return ResultWrap.init(CommonConstants.FALIED, "没有门店信息!");
					}
					
				}else {
					LOG.info("新无卡的商户门店======");
					
					List<String> ghtXwkCityMerchantCodeByProvinceAndCity = topupPayChannelBusiness.getGHTXwkCityMerchantCodeByProvinceAndCity(province.trim(), city.trim());
					
					if(bankName.contains("交通")) {
						for(String ghtxwkmc : ghtXwkCityMerchantCodeByProvinceAndCity) {
							if(!ghtxwkmc.contains("珠宝") && !ghtxwkmc.contains("黄金") && !ghtxwkmc.contains("美容") && !ghtxwkmc.contains("美发")) {
								list.add(ghtxwkmc);
							}
						}
						
						if(list != null && list.size()>0) {
							int j = random.nextInt(list.size());

							storeNo = list.get(j);
						}else {
							
							return ResultWrap.init(CommonConstants.FALIED, "没有门店信息!");
						}
					}else {
						for(String ghtxwkmc : ghtXwkCityMerchantCodeByProvinceAndCity) {
							
							list.add(ghtxwkmc);
						}
						
						if(list != null && list.size()>0) {
							int j = random.nextInt(list.size());

							storeNo = list.get(j);
						
						}else {
							
							return ResultWrap.init(CommonConstants.FALIED, "没有门店信息!");
						}
					}
				}
			}else {
				
				if ("中国银行".equals(bankName) || bankName.contains("建设") || bankName.contains("华夏")
						|| bankName.contains("兴业") || bankName.contains("中信") || bankName.contains("浦发")
						|| bankName.contains("浦东发展") || bankName.contains("广发") || bankName.contains("广东发展")
						|| bankName.contains("平安") || bankName.contains("邮储") || bankName.contains("邮政储蓄")
						|| bankName.contains("渣打") || bankName.contains("花旗") || bankName.contains("恒丰")
						|| bankName.contains("汇丰") || bankName.contains("工商") || bankName.contains("农业")) {
					
					list.add("MD0638802"); //上海银杉小吃屋
					list.add("MD0638814"); //上海伍吃美食店
					list.add("MD0645022"); //上海弘一花店
					list.add("MD0644980"); //上海市徐汇区家清百货商店
					
					storeNo = list.get(random.nextInt(list.size()));
					
				}else {
					
					list.add("MD0663583");
					list.add("MD0663595");
					list.add("MD0669803");
					list.add("MD0669761");
					
					storeNo = list.get(random.nextInt(list.size()));
					
				}
				
			}
			
		}
		
		LOG.info("orderCode======" + orderCode + "   &   storeNo======" + storeNo);
		
		if("10".equals(orderType)) {
			LOG.info("根据判断进入消费任务======");
			
			map = (Map<String, Object>) ghtdhpageRequst.ghtdhPreOrder(orderCode, expiredTime, securityCode, storeNo);
			
		}
		
		
		if("11".equals(orderType)) {
			LOG.info("根据判断进入还款任务======");
			
			map = (Map<String, Object>) ghtdhpageRequst.transfer(orderCode, extra);
			
		}
		
		return map;
	}

}
