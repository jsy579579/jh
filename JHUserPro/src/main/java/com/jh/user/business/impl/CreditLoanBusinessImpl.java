package com.jh.user.business.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.jh.user.business.CreditLoanBusiness;
import com.jh.user.pojo.CreditLoan;
import com.jh.user.repository.CreditLoanRepository;
@Service
public class CreditLoanBusinessImpl implements CreditLoanBusiness {
	
	@Autowired
	private CreditLoanRepository creditLoanRepository;

	@Override
	public CreditLoan save(CreditLoan model) {
		return creditLoanRepository.saveAndFlush(model);
	}

	@Override
	public Page<CreditLoan> findByBrandIdAndStatus(Long brandId, Integer status, Pageable pageable) {
		return creditLoanRepository.findByBrandIdAndStatus(brandId, status, pageable);
	}

	@Override
	public CreditLoan findById(Long id) {
		return creditLoanRepository.findOne(id);
	}
}
