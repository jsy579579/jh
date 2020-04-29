package com.jh.user.business.impl;

import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jh.user.business.CheckUserBusiness;
import com.jh.user.pojo.CheckUser;
import com.jh.user.repository.CheckUserRepository;
import com.jh.user.service.CheckUserService;
import com.jh.user.util.Util;

@Service
public class CheckUserBusinessImpl implements CheckUserBusiness{

	private static final Logger LOG = LoggerFactory.getLogger(CheckUserService.class);
	
	@Autowired
	private CheckUserRepository checkUserRepository;
	
	@Autowired
	private EntityManager em;
	
	@Autowired
	Util util;
	
	@Transactional
	@Override
	public CheckUser saveCheckUser(CheckUser checkUser) {
		CheckUser result = checkUserRepository.save(checkUser);
		em.flush();
		return result;
	}

	@Override
	public CheckUser queryUserById(long id) {
		
		return checkUserRepository.queryUserById(id);
	}
	
}
