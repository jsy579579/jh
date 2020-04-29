package com.jh.user.business;

import java.util.List;

import com.jh.user.pojo.UserShops;

public interface UserShopsBusiness {
	/**
	 * 查询所有商户信息
	 * */
	public List<UserShops> findUserShops();
	
	/**
	 * 通过商户Id查询商户信息
	 * */
	public UserShops findUserShopsById(long id);
	
	/**
	 * 通过userid查询商户
	 * **/
	public UserShops findUserShopsByUid(long userid);
	/**
	 * 添加商户
	 * **/
	public UserShops addUserShops(UserShops us);
	/**
	 * 修改商户审核状态
	 * **/
	public UserShops updateUserShopsStatusByUid(long userid,String status);

	//获取shops表中随机的userid
	public String[] queryRandomUseridByAll();
	
}
