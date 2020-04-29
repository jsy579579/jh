package com.jh.mircomall.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jh.mircomall.bean.Cities;
import com.jh.mircomall.dao.CitiesDao;

@Service
public class CitiesServiceImpl implements CitiesService {

	@Autowired
	private CitiesDao citiesDao;
	
	@Override
	public List<Cities> listCityByPrivincesId(String provincesId) {
		
		return citiesDao.listCityByPrivincesId(provincesId);
	}

}
