package com.jh.user.business.impl;

import java.util.List;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jh.user.business.LoanApplicationBusiness;
import com.jh.user.pojo.LoanApplication;
import com.jh.user.repository.LoanApplicationRepository;

@Service
public class LoanApplicationBusinessImpl implements LoanApplicationBusiness {

	
	@Autowired
	private EntityManager em;
	
	@Autowired
	private LoanApplicationRepository loanApplicationRepository;

	@Transactional
	@Override
	public void createLoanApplication(LoanApplication loanApplication) {
		loanApplicationRepository.saveAndFlush(loanApplication);
	}

	@Override
	public List<LoanApplication> getLoanApplicationByBrandIdAndStatus(int brandId, int status) {
		em.clear();
		List<LoanApplication> result = loanApplicationRepository.getLoanApplicationByBrandIdAndStatus(brandId, status);
		return result;
	}

	@Override
	public LoanApplication getLoanApplicationByBrandIdAndIdAndStatus(int brandId, long id, int status) {
		em.clear();
		LoanApplication result = loanApplicationRepository.getLoanApplicationByBrandIdAndIdAndStatus(brandId, id, status);
		return result;
	}

	@Override
	public List<LoanApplication> getLoanApplicationByBrandIdAndIdsAndStatus(int brandId, long[] id, int status) {
		em.clear();
		List<LoanApplication> result = loanApplicationRepository.getLoanApplicationByBrandIdAndIdsAndStatus(brandId, id, status);
		return result;
	}

	@Override
	public Page<LoanApplication> getLoanApplicationByBrandIdAndStatusAndPage(int brandId, int status, Pageable pageable) {
		em.clear();
		Page<LoanApplication> result = loanApplicationRepository.getLoanApplicationByBrandIdAndStatusAndPage(brandId,status, pageable);
		return result;
	}

	@Override
	public Page<LoanApplication> getLoanApplicationByBrandIdAndStatusAndTitleAndPage(int brandId, int status, String title,
			Pageable pageable) {
		em.clear();
		Page<LoanApplication> result = loanApplicationRepository.getLoanApplicationByBrandIdAndStatusAndTitleAndPage(brandId, status, title, pageable);
		return result;
	}
	
	
}
