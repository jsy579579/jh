package com.jh.notice.business.impl;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jh.notice.business.BrandSMSCountBusiness;
import com.jh.notice.pojo.BrandSMSCount;
import com.jh.notice.repository.BrandSMSCountRepository;


@Service
public class BrandSMSCountBusinessImpl implements BrandSMSCountBusiness {
	
	@Autowired
	private BrandSMSCountRepository brandSMSCountRepository;
	
	@Autowired
	private EntityManager em;

	@Override
	public BrandSMSCount findByBrandId(String brandId) {
		BrandSMSCount brandSMSCount = brandSMSCountRepository.findBrandSMSCount(brandId);
		em.clear();
		return brandSMSCount;
	}

	@Override
	@Transactional
	public BrandSMSCount save(BrandSMSCount brandCardAuthCount) {
		BrandSMSCount brandSMSCount = brandSMSCountRepository.save(brandCardAuthCount);
		em.flush();
		em.clear();
		return brandSMSCount;
	}

	@Override
	@Transactional
	public void delete(BrandSMSCount cardAuthCount) {
		brandSMSCountRepository.delete(cardAuthCount);
	}
}
