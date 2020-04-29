package com.jh.mircomall.service;

import java.util.Map;

import com.jh.mircomall.bean.UserAccount;

public interface UserAccountService {
	/**
	 * 根据用户ID查询用户信息
	 *@Author ChenFan
	 *@Date 2018年5月9日
	 * @param userId
	 * @return
	 */
	public  Map<Object, Object> getUserInfo(int userId);
	/**
	 * 修改用户积分
	 *@Author ChenFan
	 *@Date 2018年5月14日
	 * @param userAccount
	 * @return
	 */
	public int modifyUserAccount(UserAccount userAccount);
}
