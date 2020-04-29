package com.jh.user.business.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jh.user.business.UserAwardConfigBusiness;
import com.jh.user.pojo.UserAwardConfig;
import com.jh.user.repository.UserAwardConfigRepository;

@Service
public class UserAwardConfigBusinessImpl implements UserAwardConfigBusiness {
	
	@Autowired
	private UserAwardConfigRepository userAwardConfigRepository;

	@Override
	public UserAwardConfig findByBrandId(String brandId) {
		return userAwardConfigRepository.findByBrandId(brandId);
	}

	@Override
	public UserAwardConfig save(UserAwardConfig userAwardConfig) {
		return userAwardConfigRepository.saveAndFlush(userAwardConfig);
	}

}
