package com.jh.mircomall.dao;

import org.apache.ibatis.annotations.Param;
import com.jh.mircomall.bean.UserAccount;

public interface UserAccountDao {
	/**
	 * 根据用户Id查询用户信息
	 *@Author ChenFan
	 *@Date 2018年5月9日
	 * @param userId
	 * @return
	 */
	UserAccount selectUserCoin(@Param("userid")int userId);
	/**
	 * 修改金额
	 *@Author ChenFan
	 *@Date 2018年5月14日
	 * @param userAccount
	 * @return
	 */
	int updateUserAccount(UserAccount userAccount);
}
