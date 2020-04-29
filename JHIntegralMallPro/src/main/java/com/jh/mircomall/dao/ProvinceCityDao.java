package com.jh.mircomall.dao;

import java.util.List;

import com.jh.mircomall.bean.ProvinceCity;

public interface ProvinceCityDao {

	/**
	 * 根据省份名称联动查询城市
	 * */
	public List<ProvinceCity> listCityByPrivincesId(String privincesId);
	
}
