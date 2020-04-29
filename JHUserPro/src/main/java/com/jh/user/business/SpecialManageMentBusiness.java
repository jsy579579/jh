package com.jh.user.business;

import com.jh.user.pojo.BankCode;
import com.jh.user.pojo.SpecialManageMent;

public interface SpecialManageMentBusiness {
	
	public SpecialManageMent createSpecialManageMent(SpecialManageMent specialManageMent);

	public SpecialManageMent getSpecialManageMentByBrandId(String brandId);
	
}
