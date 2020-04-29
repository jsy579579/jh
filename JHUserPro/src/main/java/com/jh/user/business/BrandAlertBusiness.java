package com.jh.user.business;

import com.jh.user.pojo.BrandAlert;

public interface BrandAlertBusiness {
	
	public void createBrandAlert(BrandAlert brandAlert);
	
	public BrandAlert getBrandAlertByBrandIdAndType(String brandId, String type);
	
	
}
