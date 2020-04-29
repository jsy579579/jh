package com.jh.mircomall.service;

import java.util.List;

import com.jh.mircomall.bean.Cities;

public interface CitiesService {

	/**
	 * 根据省id查询城市
	 * */
	List<Cities> listCityByPrivincesId(String provincesId);
	
}
