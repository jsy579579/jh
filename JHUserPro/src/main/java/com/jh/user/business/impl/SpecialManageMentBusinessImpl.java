package com.jh.user.business.impl;

import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jh.user.business.SpecialManageMentBusiness;
import com.jh.user.pojo.SpecialManageMent;
import com.jh.user.repository.SpecialManageMentRepository;
import com.jh.user.service.CheckUserService;
import com.jh.user.util.Util;

@Service
public class SpecialManageMentBusinessImpl implements SpecialManageMentBusiness{

	private static final Logger LOG = LoggerFactory.getLogger(CheckUserService.class);
	
	@Autowired
	private SpecialManageMentRepository specialManageMentRepository;
	
	@Autowired
	private EntityManager em;
	
	@Autowired
	Util util;

	@Transactional
	@Override
	public SpecialManageMent createSpecialManageMent(SpecialManageMent specialManageMent) {
		SpecialManageMent result = specialManageMentRepository.save(specialManageMent);
		em.flush();
		return result;
	}

	@Override
	public SpecialManageMent getSpecialManageMentByBrandId(String brandId) {
		SpecialManageMent result = specialManageMentRepository.getSpecialManageMentByBrandId(brandId);
		em.clear();
		return result;
	}
	


	
}
