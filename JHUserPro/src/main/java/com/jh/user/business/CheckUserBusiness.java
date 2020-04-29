package com.jh.user.business;

import com.jh.user.pojo.CheckUser;

public interface CheckUserBusiness {
	
	//增加用户
	public CheckUser saveCheckUser(CheckUser checkUser);
	
	
	//查询用户
	public CheckUser queryUserById(long id);
}

