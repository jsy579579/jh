package com.jh.user.business;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.jh.user.pojo.CreditLoan;

public interface CreditLoanBusiness {

	CreditLoan save(CreditLoan model);

	Page<CreditLoan> findByBrandIdAndStatus(Long brandId, Integer status,Pageable pageable);

	CreditLoan findById(Long id);

}
