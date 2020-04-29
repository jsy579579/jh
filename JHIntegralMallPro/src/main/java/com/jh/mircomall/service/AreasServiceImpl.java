package com.jh.mircomall.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jh.mircomall.bean.Areas;
import com.jh.mircomall.dao.AreasDao;

@Service
public class AreasServiceImpl implements AreasService {

	@Autowired
	private AreasDao areasDao;
	@Override
	public List<Areas> getAreasList(String cityid) {
	
		return areasDao.selectAreasByCitisId(cityid);
	}

}
