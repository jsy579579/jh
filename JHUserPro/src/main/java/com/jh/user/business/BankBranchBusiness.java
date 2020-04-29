package com.jh.user.business;

import java.util.List;

import com.jh.user.pojo.Area;
import com.jh.user.pojo.BankBranch;
import com.jh.user.pojo.City;
import com.jh.user.pojo.Province;

public interface BankBranchBusiness {
	public List<BankBranch> findAllBranch(String province,String city,String bankName);
	
	public List<Province> findProvince();
	
	public List<City> findCity(String provinceid);
	
	public List<Area> findArea(String cityid);
	
	//通过银行名称查询银行编号
	public BankBranch getBankCodeByBankName(String bankName);
}
