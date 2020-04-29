package com.jh.mircomall.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.jh.mircomall.bean.ConsigneeAddress;
import com.jh.mircomall.service.ConsigneeAddressService;

import cn.jh.common.utils.CommonConstants;

@SuppressWarnings("all")
@RestController
@RequestMapping("/v1.0/integralmall/consignee/address")
public class ConsigneeAddressController {
	@Autowired
	private ConsigneeAddressService consigneeAddressService;

	/**
	 * 添加收件人地址
	 * 
	 * @param userId
	 * @param provinceId
	 * @param cityId
	 * @param areaId
	 * @param detailedAddr
	 * @param consigneePhone
	 * @param defaultAddr
	 * @param consigneeName
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/add")
	public Object addAddress(@RequestParam("userId") int userId, @RequestParam("provinceId") String provinceId,
			@RequestParam("cityId") String cityId, @RequestParam("areaId") String areaId,
			@RequestParam("detailedAddr") String detailedAddr, @RequestParam("consigneePhone") String consigneePhone,
			@RequestParam(value = "defaultAddr", defaultValue = "0") int defaultAddr,
			@RequestParam("consigneeName") String consigneeName) {
		ConsigneeAddress address = new ConsigneeAddress();
		address.setAreaid(areaId);
		address.setCityid(cityId);
		address.setConsigneeName(consigneeName);
		address.setDefaultAddr(defaultAddr);
		address.setDetailedAddr(detailedAddr);
		address.setConsigneePhone(consigneePhone);
		address.setProvinceid(provinceId);
		address.setUserId(userId);
		Date now = new Date();
		address.setChangeTime(now);
		address.setCreateTime(now);
		int isSuccess = consigneeAddressService.addConsigneeAddress(address);
		Map map = new HashMap();
		if (isSuccess > 0) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, "成功");
			map.put(CommonConstants.RESULT, isSuccess);
		} else {
			map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_PARAM);
			map.put(CommonConstants.RESP_MESSAGE, "失败");
			map.put(CommonConstants.RESULT, isSuccess);
		}
		return map;
	}

	@RequestMapping(value = "/getAddress", method = RequestMethod.POST)
	public Object getConsigneeAddress(@RequestParam("userid") int userId) {
		List<ConsigneeAddress> list = consigneeAddressService.getAllVonsigneeAddress(userId, 0);
		Map map = new HashMap();
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		map.put(CommonConstants.RESULT, list);
		return map;
	}

	@RequestMapping(value = "/removeAddress", method = RequestMethod.POST)
	public Object deleteConsignAddress(@RequestParam("userid") int userId, @RequestParam("id") int id) {
		int isSuccess = consigneeAddressService.removeConsignAddress(userId, id);
		Map map = new HashMap();
		if (isSuccess > 0) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, "成功");
			map.put(CommonConstants.RESULT, isSuccess);
		} else {
			map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_PARAM);
			map.put(CommonConstants.RESP_MESSAGE, "失败");
			map.put(CommonConstants.RESULT, isSuccess);
		}
		return map;
	}

	@RequestMapping(value = "/modifyAddress", method = RequestMethod.POST)
	public Object updateConsigneeAddress(@RequestParam("id") int id, @RequestParam("provinceid") String provinceId,
			@RequestParam("cityid") String cityid, @RequestParam("areaid") String areaid,
			@RequestParam("detailedaddr") String detailedAddr, @RequestParam("phone") String consigneePhone,
			@RequestParam(value = "defaultaddr", defaultValue = "0") int defaultAddr,
			@RequestParam("consigneename") String consigneeName, @RequestParam("userId") Integer userId) {
		ConsigneeAddress address = new ConsigneeAddress();
		address.setAreaid(areaid);
		address.setCityid(cityid);
		address.setConsigneeName(consigneeName);
		address.setDefaultAddr(defaultAddr);
		address.setDetailedAddr(detailedAddr);
		address.setConsigneePhone(consigneePhone);
		address.setProvinceid(provinceId);
		address.setId(id);
		address.setUserId(userId);
		int isSuccess = consigneeAddressService.modifydefaultAddress(address);
		Map map = new HashMap();
		if (isSuccess > 0) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, "成功");
			map.put(CommonConstants.RESULT, isSuccess);
		} else {
			map.put(CommonConstants.RESP_CODE, CommonConstants.ERROR_PARAM);
			map.put(CommonConstants.RESP_MESSAGE, "失败");
			map.put(CommonConstants.RESULT, isSuccess);
		}
		return map;
	}

	/**
	 * 得到用户的默认地址
	 * 
	 * @param userId
	 * @return
	 */
	@RequestMapping(value = "/getDefault", method = RequestMethod.POST)
	public Object getDefaultConsignesAddress(@RequestParam("userid") int userId) {
		// 获取用户的默认地址
		ConsigneeAddress consigneeAddress = consigneeAddressService.getConsigneeAddress(userId, 1);
		Map map = new HashMap();
		if (consigneeAddress == null) {
			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, "成功");
			map.put(CommonConstants.RESULT, new ConsigneeAddress());
			map.put("defaultLogo", "0");
		} else {
			map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
			map.put(CommonConstants.RESP_MESSAGE, "成功");
			map.put(CommonConstants.RESULT,consigneeAddress);
			map.put("defaultLogo", "1");
		}
		return map;
	}
}