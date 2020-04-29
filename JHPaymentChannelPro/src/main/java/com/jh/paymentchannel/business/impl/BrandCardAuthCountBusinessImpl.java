package com.jh.paymentchannel.business.impl;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jh.paymentchannel.business.BrandCardAuthCountBusiness;
import com.jh.paymentchannel.pojo.BrandCardAuthCount;
import com.jh.paymentchannel.repository.BrandCardAuthCountRepository;

@Service
public class BrandCardAuthCountBusinessImpl implements BrandCardAuthCountBusiness {
	
	@Autowired
	private BrandCardAuthCountRepository brandCardAuthCountRepository;
	
	@Autowired
	private EntityManager em;

	@Override
	public BrandCardAuthCount findByBrandId(String brandId) {
		BrandCardAuthCount brandCardAuthCount = brandCardAuthCountRepository.findByBrandId(brandId);
		em.clear();
		return brandCardAuthCount;
	}

	@Override
	@Transactional
	public BrandCardAuthCount save(BrandCardAuthCount brandCardAuthCount) {
		BrandCardAuthCount cardAuthCount = brandCardAuthCountRepository.save(brandCardAuthCount);
		em.flush();
		em.clear();
		return cardAuthCount;
	}

	@Override
	@Transactional
	public void delete(BrandCardAuthCount cardAuthCount) {
		brandCardAuthCountRepository.delete(cardAuthCount);
	}
}
