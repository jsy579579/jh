package com.jh.user.business.impl;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jh.user.business.UserBankCardLimitBusiness;
import com.jh.user.pojo.UserBankCardLimit;
import com.jh.user.repository.UserBankCardLimitRepository;
@Service
public class UserBankCardLimitBusinessImpl implements UserBankCardLimitBusiness {
	@Autowired
	private UserBankCardLimitRepository userBankCardLimitRepository;
	@Transactional
	@Override
	public UserBankCardLimit save(UserBankCardLimit model) {
		return userBankCardLimitRepository.save(model);
	}

	@Override
	public int queryTodayCount(long userId) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		return userBankCardLimitRepository.queryTodayCount(userId,sdf.format(new Date()));
	}

	@Override
	public int queryTodySameCount(long userId, String idcard,String bankCard) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		return userBankCardLimitRepository.queryTodySameCount(userId,idcard,sdf.format(new Date()),bankCard);
	}
	
}
