package com.jh.user.business.impl;

import java.util.List;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jh.user.business.BankBranchBusiness;
import com.jh.user.pojo.Area;
import com.jh.user.pojo.BankBranch;
import com.jh.user.pojo.City;
import com.jh.user.pojo.Province;
import com.jh.user.repository.BankBranchRepository;
@Service
public class BankBranchImpl implements BankBranchBusiness{
	@Autowired
	private BankBranchRepository bbr;
	@Autowired
	private EntityManager em;
	@Override
	public List<BankBranch> findAllBranch(String province, String city, String topName) {
		// TODO Auto-generated method stub
		return bbr.querybranchinfo(province, city, topName);
		//return null;
	}
	@Override
	public List<Province> findProvince() {
		// TODO Auto-generated method stub
		return bbr.findAllProvince();
	}
	@Override
	public List<City> findCity(String provinceid) {
		// TODO Auto-generated method stub
		return bbr.findAllCity(provinceid);
	}
	@Override
	public List<Area> findArea(String cityid) {
		// TODO Auto-generated method stub
		return bbr.findAllArea(cityid);
	}
	
	//根据银行名称获取银行编号
	@Override
	public BankBranch getBankCodeByBankName(String bankName) {
		return bbr.getBankCodeByBankName(bankName);
	}

}
