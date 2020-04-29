package com.jh.user.business.impl;

import java.util.List;

import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jh.user.business.CheckUserOtherBusiness;
import com.jh.user.pojo.CheckUserOther;
import com.jh.user.repository.CheckUserOtherRepository;
import com.jh.user.service.CheckUserOtherService;
import com.jh.user.util.Util;

@Service
public class CheckUserOtherBusinessImpl implements CheckUserOtherBusiness {
	
	private static final Logger LOG = LoggerFactory.getLogger(CheckUserOtherService.class);
	
	@Autowired
	private CheckUserOtherRepository checkUserOtherRepository;
	
	@Autowired
	EntityManager em;
	
	@Autowired
	Util util;
	
	@Transactional
	@Override
	public CheckUserOther saveCheckUserOther(CheckUserOther checkUserOther) {
		CheckUserOther result = checkUserOtherRepository.save(checkUserOther);
		em.flush();
		return result;
	}

	@Override
	public Page<CheckUserOther> queryUserById(Pageable pageAble) {
		
		return checkUserOtherRepository.queryUserById(pageAble);
	}


	
	
	
	
}
