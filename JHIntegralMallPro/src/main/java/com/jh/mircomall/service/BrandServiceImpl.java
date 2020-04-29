package com.jh.mircomall.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jh.mircomall.bean.Brand;
import com.jh.mircomall.dao.BrandDao;

@Service
public class BrandServiceImpl implements BrandService {

	@Autowired
	private BrandDao brandDao;
	
	@Override
	public List<Brand> findbusinessById(Integer brandId) {
		
		return brandDao.findbusinessById(brandId);
	}

}
