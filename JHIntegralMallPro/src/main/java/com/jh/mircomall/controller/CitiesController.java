package com.jh.mircomall.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jh.mircomall.bean.Cities;
import com.jh.mircomall.service.CitiesService;

import cn.jh.common.utils.CommonConstants;

@RestController
@RequestMapping("/v1.0/integralmall/cities")
@SuppressWarnings("all")
public class CitiesController {
	@Autowired
	private CitiesService citiesService;
	
	@RequestMapping(value="get",method=RequestMethod.POST)
	public Object getCities(@RequestParam("provincesid") String provincesId){
		List<Cities> citiesList = citiesService.listCityByPrivincesId(provincesId);
		Map map = new HashMap();
		map.put(CommonConstants.RESP_CODE, CommonConstants.SUCCESS);
		map.put(CommonConstants.RESP_MESSAGE, "成功");
		map.put(CommonConstants.RESULT, citiesList);
		return map;
	}
}
