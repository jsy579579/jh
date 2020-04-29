package com.jh.paymentchannel.service;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.jh.common.utils.CommonConstants;

import com.jh.paymentchannel.business.BrandManageBusiness;
import com.jh.paymentchannel.pojo.SwiftBrandMerchant;

@Controller
@EnableAutoConfiguration
public class BrandManageService {

	
	private static final Logger log = LoggerFactory.getLogger(BrandManageService.class);
	
	@Autowired
	private BrandManageBusiness brandManageBusiness;
	
	
	/**银行四要素验证**/
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/paymentchannel/brand/submerchant")
	public @ResponseBody Object bandcard4Auth(HttpServletRequest request, 
			@RequestParam(value = "brand_id") String brandid){
		SwiftBrandMerchant merchant = brandManageBusiness.getSwiftBrandMerchant(brandid);
		if(merchant == null){
			merchant.setSubMerchantid("105540022779");
			merchant.setSubMerchantKey("a2e1bd82e2fec0f82b0ebfa19544d36d");
		}
		Map map = new HashMap();
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESULT, merchant);
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		return map;
	}
	
	
	//配置新贴牌扫码通道的商户资质
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/paymentchannel/brand/addsubmerchant")
	public @ResponseBody Object AddSubmerChant(HttpServletRequest request,
			@RequestParam(value = "brand_id") String brandId) {
		
		Map map = new HashMap();
		
		SwiftBrandMerchant swiftBrandMerchant = brandManageBusiness.getSwiftBrandMerchant("2");
		String subMerchantid = swiftBrandMerchant.getSubMerchantid();
		String subMerchantKey = swiftBrandMerchant.getSubMerchantKey();
		
		SwiftBrandMerchant swiftBrandMerchant1 = new SwiftBrandMerchant();
		swiftBrandMerchant1.setBrand_id(brandId);
		swiftBrandMerchant1.setSubMerchantid(subMerchantid);
		swiftBrandMerchant1.setSubMerchantKey(subMerchantKey);
		
		try {
			brandManageBusiness.createSwiftBrandMerchant(swiftBrandMerchant1);
		} catch (Exception e) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.FALIED);
			map.put(CommonConstants.RESP_MESSAGE, "失败");
			return map;
		}
		
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		return map;
	}
	
}
