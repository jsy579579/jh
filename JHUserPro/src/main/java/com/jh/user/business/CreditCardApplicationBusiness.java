package com.jh.user.business;

import com.jh.user.pojo.CreditCardApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CreditCardApplicationBusiness {
	
	public void createCreditCardApplication(CreditCardApplication creditCardApplication);
	
	public List<CreditCardApplication> getCreditCardApplicationByBrandIdAndStatus(int brandId, int status);
	
	public CreditCardApplication getCreditCardApplicationByBrandIdAndIdAndStatus(int brandId, long id, int status);
	
	public List<CreditCardApplication> getCreditCardApplicationByBrandIdAndIdsAndStatus(int brandId, long[] id, int status);
	
	public Page<CreditCardApplication> getCreditCardApplicationByBrandIdAndStatusAndPage(int brandId, int status, Pageable pageable);
	
	public Page<CreditCardApplication> getCreditCardApplicationByBrandIdAndTitleAndStatusAndPage(int brandId, String title, int status, Pageable pageable);

}
