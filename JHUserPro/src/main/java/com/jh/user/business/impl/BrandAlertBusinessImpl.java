package com.jh.user.business.impl;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jh.user.business.BrandAlertBusiness;
import com.jh.user.pojo.BrandAlert;
import com.jh.user.repository.BrandAlertRepository;

@Service
public class BrandAlertBusinessImpl implements BrandAlertBusiness {

	@Autowired
	private EntityManager em;

	@Autowired
	private BrandAlertRepository brandAlertRepository;
	
	@Transactional
	@Override
	public void createBrandAlert(BrandAlert brandAlert) {
		brandAlertRepository.saveAndFlush(brandAlert);
	}

	@Override
	public BrandAlert getBrandAlertByBrandIdAndType(String brandId, String type) {
		em.clear();
		BrandAlert result = brandAlertRepository.getBrandAlertByBrandIdAndType(brandId, type);
		return result;
	}

	
	
}
