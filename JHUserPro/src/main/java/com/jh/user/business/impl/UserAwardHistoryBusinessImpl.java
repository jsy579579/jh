package com.jh.user.business.impl;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jh.user.business.UserAwardHistoryBusiness;
import com.jh.user.pojo.UserAwardHistory;
import com.jh.user.repository.UserAwardHistoryRepository;

@Service
public class UserAwardHistoryBusinessImpl implements UserAwardHistoryBusiness {
	
	@Autowired
	private UserAwardHistoryRepository userAwardHistoryRepository;

	@Override
	public UserAwardHistory save(UserAwardHistory userAwardHistory) {
		return userAwardHistoryRepository.saveAndFlush(userAwardHistory);
	}

	@Override
	public UserAwardHistory findByUserIdAndAwardMoney(String userId, BigDecimal awardMoney) {
		return userAwardHistoryRepository.findByUserIdAndAwardMoney(userId, awardMoney);
	}

}
