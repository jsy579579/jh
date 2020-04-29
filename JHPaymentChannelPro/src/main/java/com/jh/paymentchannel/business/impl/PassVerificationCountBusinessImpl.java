package com.jh.paymentchannel.business.impl;


import java.util.List;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jh.paymentchannel.business.PassVerificationBusiness;
import com.jh.paymentchannel.business.PassVerificationCountBusiness;
import com.jh.paymentchannel.pojo.PassVerification;
import com.jh.paymentchannel.pojo.PassVerificationCount;
import com.jh.paymentchannel.repository.PassRepository;
import com.jh.paymentchannel.repository.PassVerificationCountRepository;
@Service
public class PassVerificationCountBusinessImpl implements PassVerificationCountBusiness{
	@Autowired
	PassVerificationCountRepository passVerificationCountRepository;
	
	@Autowired
	private EntityManager em;
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Transactional
	@Override
	public PassVerificationCount save(PassVerificationCount model) {
		PassVerificationCount pvf = passVerificationCountRepository.save(model);
		em.flush();
		em.clear();
		return pvf;
	}

    
	@Override
	public PassVerificationCount findPassByUserId(long userid) {
		return passVerificationCountRepository.findByUserId(userid);
		
	}


	@Override
	public List<PassVerificationCount> findPassByBrandid(long brandid) {
		// TODO Auto-generated method stub
		return passVerificationCountRepository.findBybrandId(brandid);
	}

	

}
