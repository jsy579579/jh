package com.jh.mircomall.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jh.mircomall.bean.ShipperCode;
import com.jh.mircomall.service.ShipperCodeService;

import cn.jh.common.utils.CommonConstants;
@SuppressWarnings("all")
@RestController
@RequestMapping("/v1.0/integralmall/shipper")
public class ShipperCodeController {
	@Autowired
	 private ShipperCodeService shipperCodeService;
	@RequestMapping("/getAllShipperCode")
	public Object getShipperCode(){
		List<ShipperCode> shipperCode = shipperCodeService.getShipperCode();
		Map map = new HashMap();
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		map.put(CommonConstants.RESULT, shipperCode);
		return map;
	}
}
