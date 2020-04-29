package com.jh.mircomall.service;

import java.util.List;
import java.util.Map;

import com.jh.mircomall.bean.Business;

public interface BusinessService {

	/**
	 * 商家开户
	 * */
	int addBusiness(Business business);
	
	/**
	 * 商家登录
	 * */
	public List<Business> loginBusiness(Map map);
	
	/**
	 * 商家注销
	 * */
	public int deleteBusiness(Map map);
	
	/**
	 *  修改商家登录密码
	 * */
	public int updateBusinessPWD(Map map);
	
	/**
	 * 分页显示所有商户信息
	 * */
	public List<Business> listAllBusiness(int currPage,int pageSize);
	
}
