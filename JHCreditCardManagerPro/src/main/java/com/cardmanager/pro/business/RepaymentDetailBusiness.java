package com.cardmanager.pro.business;

import com.cardmanager.pro.pojo.RepaymentBrandStatus;

public interface RepaymentDetailBusiness {
	
	
	public void createRepaymentBrandStatus(RepaymentBrandStatus repaymentBrandStatus);
	
	public RepaymentBrandStatus getRepaymentBrandStatusByBrandIdAndVersion(int brandId, String version);
	
	
}
