package com.jh.user.business;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.jh.user.pojo.LoanApplication;

public interface LoanApplicationBusiness {
	
	public void createLoanApplication(LoanApplication loanApplication);
	
	public List<LoanApplication> getLoanApplicationByBrandIdAndStatus(int brandId, int status);
	
	public LoanApplication getLoanApplicationByBrandIdAndIdAndStatus(int brandId, long id, int status);
	
	public List<LoanApplication> getLoanApplicationByBrandIdAndIdsAndStatus(int brandId, long[] id, int status);
	
	public Page<LoanApplication> getLoanApplicationByBrandIdAndStatusAndPage(int brandId, int status, Pageable pageable);
	
	public Page<LoanApplication> getLoanApplicationByBrandIdAndStatusAndTitleAndPage(int brandId, int status, String title, Pageable pageable);
	
}
