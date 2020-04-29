package com.jh.user.business;

import java.math.BigDecimal;

import com.jh.user.pojo.UserAwardHistory;

public interface UserAwardHistoryBusiness {

	UserAwardHistory save(UserAwardHistory userAwardHistory);

	UserAwardHistory findByUserIdAndAwardMoney(String userId, BigDecimal awardMoney);

}
