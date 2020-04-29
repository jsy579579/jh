package com.jh.notice.business;

import com.jh.notice.pojo.BrandSMSCount;

public interface BrandSMSCountBusiness {

	BrandSMSCount findByBrandId(String brandId);

	BrandSMSCount save(BrandSMSCount brandSMSCount);

	void delete(BrandSMSCount brandSMSCount);

}
