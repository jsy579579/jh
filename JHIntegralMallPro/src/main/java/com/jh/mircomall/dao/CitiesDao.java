package com.jh.mircomall.dao;

import java.util.List;

import com.jh.mircomall.bean.Cities;

public interface CitiesDao {
	
	/**
	 * 根据省id查询城市
	 * */
	List<Cities> listCityByPrivincesId(String provincesId);
}