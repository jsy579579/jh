package com.jh.mircomall.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jh.mircomall.bean.Provinces;
import com.jh.mircomall.dao.ProvincesDao;

@Service
public class ProvincesServiceImpl implements ProvincesService {

	@Autowired
	private ProvincesDao provincesDao;
	
	@Override
	public List<Provinces> listAllProvinces() {
		return provincesDao.listAllProvinces();
	}

}
