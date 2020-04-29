package com.jh.user.business;

import com.jh.user.pojo.UserBankCardLimit;

public interface UserBankCardLimitBusiness {

	UserBankCardLimit save(UserBankCardLimit model);

	int queryTodayCount(long userId);

	int queryTodySameCount(long userId, String idcard,String bankCard);
	
}
