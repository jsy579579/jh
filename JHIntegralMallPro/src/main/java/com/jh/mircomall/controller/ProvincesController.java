package com.jh.mircomall.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.jh.mircomall.bean.Provinces;
import com.jh.mircomall.service.ProvincesService;

import cn.jh.common.utils.CommonConstants;
@SuppressWarnings("all")
@RestController
@RequestMapping("/v1.0/integralmall/provinces")
public class ProvincesController {
	@Autowired
	private ProvincesService provincesService;
	@RequestMapping(value="/get",method=RequestMethod.POST)
	public Object getProvinces(){
		List<Provinces> provinces = provincesService.listAllProvinces();
		Map map = new HashMap();
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		map.put(CommonConstants.RESULT, provinces);
		return map;
	}
}
