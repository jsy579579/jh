package com.jh.mircomall.dao;

import java.util.List;
import java.util.Map;

import com.jh.mircomall.bean.Business;

public interface BusinessDao {

	/**
	 * 商家开户
	 * username:用户名
	 * password:密码
	 * brandId:贴牌id
	 * phone:商家电话
	 * addr:商家地址
	 * goodsParentId:主营商品
	 * createTime:创建时间
	 * */
	public int addBusiness(Business business);
	
	/**
	 * 商家登录
	 * username:用户名
	 * password:密码
	 * */
	public List<Business> loginBusiness(Map map);
	
	/**
	 * 商家注销
	 * */
	public int deleteBusiness(Map map);
	
	/**
	 * 修改商家登录密码
	 * */
	public int updateBusinessPWD(Map map);
	
	/**
	 * 显示所有商户信息
	 * */
	public List<Business> listAllBusiness(int currPage,int pageSize);
	
}
