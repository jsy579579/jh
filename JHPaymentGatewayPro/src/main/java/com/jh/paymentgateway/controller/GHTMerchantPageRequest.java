package com.jh.paymentgateway.controller;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jh.paymentgateway.basechannel.BaseChannel;
import com.jh.paymentgateway.business.TopupPayChannelBusiness;
import com.jh.paymentgateway.config.RedisUtil;
import com.jh.paymentgateway.pojo.GHTCityMerchant;
import com.jh.paymentgateway.pojo.GHTXwkCityMerchant;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;

@Controller
@EnableAutoConfiguration
public class GHTMerchantPageRequest extends BaseChannel {
	private static final Logger LOG = LoggerFactory.getLogger(GHTMerchantPageRequest.class);

	@Autowired
	private RedisUtil redisUtil;

	@Autowired
	private TopupPayChannelBusiness topupPayChannelBusiness;

	@Value("${payment.ipAddress}")
	private String ip;

	//获取省份的接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/ghtmerchant/getghtprovince")
	public @ResponseBody Object getGHTProvince(HttpServletRequest request
			) throws Exception {
		
		List<String> ghtCityMerchantProvince = topupPayChannelBusiness.getGHTCityMerchantProvince();
	
		return ResultWrap.init(CommonConstants.SUCCESS, "查询省份成功", ghtCityMerchantProvince);

	}

	//根据省份获取城市的接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/ghtmerchant/getghtcitybyprovince")
	public @ResponseBody Object getGHTCityByProvince(HttpServletRequest request,
			@RequestParam(value = "province") String province
			) throws Exception {
		
		List<String> ghtCityMerchantCityByProvince = topupPayChannelBusiness.getGHTCityMerchantCityByProvince(province.trim());
		
		HashSet set = new HashSet(ghtCityMerchantCityByProvince);
		
		ghtCityMerchantCityByProvince.clear();
		ghtCityMerchantCityByProvince.addAll(set);
	
		return ResultWrap.init(CommonConstants.SUCCESS, "查询省份成功", ghtCityMerchantCityByProvince);

	}
	
	
	
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/ghtmerchant/getghtmerchantbyprovinceandcity")
	public @ResponseBody Object getGHTMerchantByProvinceAndCity(HttpServletRequest request,
			@RequestParam(value = "province") String province,
			@RequestParam(value = "city") String city,
			@RequestParam(value = "bankName") String bankName,
			@RequestParam(value = "size", defaultValue = "20", required = false) int size
			) throws Exception {
		
		if("中国银行".equals(bankName) || bankName.contains("建设") || bankName.contains("华夏")
				|| bankName.contains("兴业") || bankName.contains("中信") || bankName.contains("浦发")
				|| bankName.contains("浦东发展") || bankName.contains("广发") || bankName.contains("广东发展")
				|| bankName.contains("平安") || bankName.contains("邮储") || bankName.contains("邮政储蓄")
				|| bankName.contains("渣打") || bankName.contains("花旗") || bankName.contains("恒丰")
				|| bankName.contains("汇丰") || bankName.contains("工商")) {
			LOG.info("全渠道支持的商户======");
			
			List<GHTCityMerchant> ghtCityMerchantByProvinceAndCity = topupPayChannelBusiness.getGHTCityMerchantByProvinceAndCity(province.trim(), city.trim());
			
			
			if (ghtCityMerchantByProvinceAndCity != null) {

				return ResultWrap.init(CommonConstants.SUCCESS, "查询成功", ghtCityMerchantByProvinceAndCity);
			} else {

				return ResultWrap.init(CommonConstants.FALIED, "暂无该城市的商户门店,请选择其他城市!");
			}
			
		}else {
			LOG.info("新无卡支持的商户======");
			
			List<GHTXwkCityMerchant> ghtXwkCityMerchantByProvinceAndCity = topupPayChannelBusiness.getGHTXwkCityMerchantByProvinceAndCity(province.trim(), city.trim());
			List<GHTXwkCityMerchant> ghtXwkCityMerchant = new ArrayList<GHTXwkCityMerchant>();
			
			if (ghtXwkCityMerchantByProvinceAndCity != null) {

				if(bankName.contains("交通")) {
					for(GHTXwkCityMerchant gcm : ghtXwkCityMerchantByProvinceAndCity) {
						String merchantName = gcm.getMerchantName();
						if(!merchantName.contains("珠宝") && !merchantName.contains("黄金") && !merchantName.contains("美容") && !merchantName.contains("美发")) {
							ghtXwkCityMerchant.add(gcm);
						}
					}
					
					if(ghtXwkCityMerchant != null) {
						
						return ResultWrap.init(CommonConstants.SUCCESS, "查询成功", ghtXwkCityMerchant);
					}else {
						
						return ResultWrap.init(CommonConstants.FALIED, "暂无该城市的商户门店,请选择其他城市!");
					}
					
				}else {
					
					return ResultWrap.init(CommonConstants.SUCCESS, "查询成功", ghtXwkCityMerchantByProvinceAndCity);
				}
			} else {

				return ResultWrap.init(CommonConstants.FALIED, "暂无该城市的商户门店,请选择其他城市!");
			}
			
		}
		
	}
	
	

}
