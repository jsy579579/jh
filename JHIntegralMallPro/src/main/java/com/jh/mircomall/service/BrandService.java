package com.jh.mircomall.service;

import java.util.List;
import java.util.Map;

import com.jh.mircomall.bean.Brand;

public interface BrandService {

	/**
	 * 根据brand_id查询贴牌商
	 * */
	List<Brand> findbusinessById(Integer brandId);
	
}
