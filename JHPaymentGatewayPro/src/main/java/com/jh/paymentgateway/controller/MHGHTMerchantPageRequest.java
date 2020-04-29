package com.jh.paymentgateway.controller;

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
import com.jh.paymentgateway.business.MHTopupPayChannelBusiness;
import com.jh.paymentgateway.pojo.MHGHTCityMerchant;
import com.jh.paymentgateway.pojo.MHGHTXwkCityMerchant;

import cn.jh.common.tools.ResultWrap;
import cn.jh.common.utils.CommonConstants;

@Controller
@EnableAutoConfiguration
public class MHGHTMerchantPageRequest extends BaseChannel {
	private static final Logger LOG = LoggerFactory.getLogger(MHGHTMerchantPageRequest.class);

	@Autowired
	private MHTopupPayChannelBusiness topupPayChannelBusiness;

	@Value("${payment.ipAddress}")
	private String ip;

	//获取省份的接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/mhghtmerchant/getghtprovince")
	public @ResponseBody Object getGHTProvince(HttpServletRequest request
			) throws Exception {
		
		List<String> ghtCityMerchantProvince = topupPayChannelBusiness.getMHGHTCityMerchantProvince();
	
		return ResultWrap.init(CommonConstants.SUCCESS, "查询省份成功", ghtCityMerchantProvince);

	}

	//根据省份获取城市的接口
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/mhghtmerchant/getghtcitybyprovince")
	public @ResponseBody Object getGHTCityByProvince(HttpServletRequest request,
			@RequestParam(value = "province") String province
			) throws Exception {
		
		List<String> ghtCityMerchantCityByProvince = topupPayChannelBusiness.getMHGHTCityMerchantCityByProvince(province.trim());
		
		HashSet set = new HashSet(ghtCityMerchantCityByProvince);
		
		ghtCityMerchantCityByProvince.clear();
		ghtCityMerchantCityByProvince.addAll(set);
	
		return ResultWrap.init(CommonConstants.SUCCESS, "查询省份成功", ghtCityMerchantCityByProvince);

	}
	
	
	
	@RequestMapping(method = RequestMethod.POST, value = "/v1.0/paymentgateway/topup/mhghtmerchant/getghtmerchantbyprovinceandcity")
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
			
			List<MHGHTCityMerchant> ghtCityMerchantByProvinceAndCity = topupPayChannelBusiness.getMHGHTCityMerchantByProvinceAndCity(province.trim(), city.trim());
			
			
			if (ghtCityMerchantByProvinceAndCity != null) {

				return ResultWrap.init(CommonConstants.SUCCESS, "查询成功", ghtCityMerchantByProvinceAndCity);
			} else {

				return ResultWrap.init(CommonConstants.FALIED, "暂无该城市的商户门店,请选择其他城市!");
			}
			
		}else {
			LOG.info("新无卡支持的商户======");
			
			List<MHGHTXwkCityMerchant> ghtXwkCityMerchantByProvinceAndCity = topupPayChannelBusiness.getMHGHTXwkCityMerchantByProvinceAndCity(province.trim(), city.trim());
			
			if (ghtXwkCityMerchantByProvinceAndCity != null) {

				return ResultWrap.init(CommonConstants.SUCCESS, "查询成功", ghtXwkCityMerchantByProvinceAndCity);
			} else {

				return ResultWrap.init(CommonConstants.FALIED, "暂无该城市的商户门店,请选择其他城市!");
			}
			
		}
		
	}
	
	

}
