package com.jh.channel.service;

import java.util.Date;
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
import cn.jh.common.utils.UUIDGenerator;

import com.jh.channel.business.OutMerchantSecurityBusiness;
import com.jh.channel.pojo.OutMerchantSecurityKey;
import com.jh.channel.util.Util;

@Controller
@EnableAutoConfiguration
public class OutMerchantSecurityKeyService {

private static final Logger LOG = LoggerFactory.getLogger(OutMerchantSecurityKeyService.class);
	
	@Autowired
	Util util;
	
	@Autowired
	private OutMerchantSecurityBusiness securityBusiness;
	
	/****
	 * 外放商户
	 * 添加和修改
	 * 
	 * **/
	@RequestMapping(method=RequestMethod.POST,value="/v1.0/channel/merchant/add")
	public @ResponseBody Object queryOrderBycode(HttpServletRequest request,   		 
			 @RequestParam(value = "phone",  required=false) String phone,
			 @RequestParam(value = "user_id") long userId,
			 @RequestParam(value = "brand_id",  required=false) String brandId
			 ){
		Map map = new HashMap();
		OutMerchantSecurityKey outMerchantSecurityKey=securityBusiness.findOutMerchantSecurityKeyByUid(userId);
		
		if(outMerchantSecurityKey==null) {
			outMerchantSecurityKey= new OutMerchantSecurityKey();
			outMerchantSecurityKey.setCreateTime(new Date());
		}
		outMerchantSecurityKey.setBrandId(brandId);
		outMerchantSecurityKey.setUserId(userId);
		outMerchantSecurityKey.setPhone(phone);
		outMerchantSecurityKey.setUpdateTime(new Date());
		outMerchantSecurityKey.setKey(UUIDGenerator.getUUID().substring(0, 16));
		map.put(CommonConstants.RESP_CODE,CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "添加成功");
		map.put(CommonConstants.RESULT,securityBusiness.addOutMerchantSecurityKey(outMerchantSecurityKey));
		return map;
	}
	
}
