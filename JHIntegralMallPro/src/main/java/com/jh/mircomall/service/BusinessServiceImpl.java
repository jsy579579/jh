package com.jh.mircomall.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jh.mircomall.bean.Business;
import com.jh.mircomall.dao.BusinessDao;

@Service
public class BusinessServiceImpl implements BusinessService {

	@Autowired
	private BusinessDao businessDao;
	
	@Override
	public int addBusiness(Business business) {
		
		return businessDao.addBusiness(business);
	}

	@Override
	public List<Business> loginBusiness(Map map) {
		// TODO Auto-generated method stub
		return businessDao.loginBusiness(map);
	}

	@Override
	public int deleteBusiness(Map map) {
		// TODO Auto-generated method stub
		return businessDao.deleteBusiness(map);
	}

	@Override
	public int updateBusinessPWD(Map map) {
		// TODO Auto-generated method stub
		return businessDao.updateBusinessPWD(map);
	}

	@Override
	public List<Business> listAllBusiness(int currPage,int pageSize) {
		
		List<Business> list = businessDao.listAllBusiness(currPage,pageSize);
		if (pageSize>list.size()) {
			
		}
//      从第几条数据开始
		int firstIndex = (currPage - 1) * pageSize;
//      到第几条数据结束
		int lastIndex = currPage * pageSize;
		return list.subList(firstIndex, lastIndex);
	}

}
