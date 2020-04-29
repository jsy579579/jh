package com.jh.mircomall.dao;

import java.util.List;
import java.util.Map;

import com.jh.mircomall.bean.Brand;

public interface BrandDao {

	/**
	 * 根据brand_id查询贴牌商
	 * */
	public List<Brand> findbusinessById(Integer brandId);
}
