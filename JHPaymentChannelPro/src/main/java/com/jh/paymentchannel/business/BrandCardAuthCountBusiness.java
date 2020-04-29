package com.jh.paymentchannel.business;

import com.jh.paymentchannel.pojo.BrandCardAuthCount;

public interface BrandCardAuthCountBusiness {

	BrandCardAuthCount findByBrandId(String brandId);

	BrandCardAuthCount save(BrandCardAuthCount brandCardAuthCount);

	void delete(BrandCardAuthCount cardAuthCount);

}
