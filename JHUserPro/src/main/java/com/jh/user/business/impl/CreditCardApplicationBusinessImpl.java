package com.jh.user.business.impl;

import com.jh.user.business.CreditCardApplicationBusiness;
import com.jh.user.pojo.CreditCardApplication;
import com.jh.user.repository.CreditCardApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;

@Service
public class CreditCardApplicationBusinessImpl implements CreditCardApplicationBusiness {

	@Autowired
	private EntityManager em;
	
	@Autowired
	private CreditCardApplicationRepository creditCardApplicationRepository;

	@Transactional
	@Override
	public void createCreditCardApplication(CreditCardApplication creditCardApplication) {
		creditCardApplicationRepository.saveAndFlush(creditCardApplication);
	}

	@Override
	public List<CreditCardApplication> getCreditCardApplicationByBrandIdAndStatus(int brandId, int status) {
		em.clear();
		List<CreditCardApplication> result = creditCardApplicationRepository.getCreditCardApplicationByBrandIdAndStatus(brandId, status);
		return result;
	}

	@Override
	public CreditCardApplication getCreditCardApplicationByBrandIdAndIdAndStatus(int brandId, long id, int status) {
		em.clear();
		CreditCardApplication result = creditCardApplicationRepository.getCreditCardApplicationByBrandIdAndIdAndStatus(brandId, id, status);
		return result;
	}

	@Override
	public List<CreditCardApplication> getCreditCardApplicationByBrandIdAndIdsAndStatus(int brandId, long[] id,
			int status) {
		em.clear();
		List<CreditCardApplication> result = creditCardApplicationRepository.getCreditCardApplicationByBrandIdAndIdsAndStatus(brandId, id, status);
		return result;
	}

	@Override
	public Page<CreditCardApplication> getCreditCardApplicationByBrandIdAndStatusAndPage(int brandId, int status,
			Pageable pageable) {
		em.clear();
		Page<CreditCardApplication> result = creditCardApplicationRepository.getCreditCardApplicationByBrandIdAndStatusAndPage(brandId, status, pageable);
		return result;
	}

	@Override
	public Page<CreditCardApplication> getCreditCardApplicationByBrandIdAndTitleAndStatusAndPage(int brandId,
			String title, int status, Pageable pageable) {
		em.clear();
		Page<CreditCardApplication> result = creditCardApplicationRepository.getCreditCardApplicationByBrandIdAndTitleAndStatusAndPage(brandId, title, status, pageable);
		return result;
	}

}
