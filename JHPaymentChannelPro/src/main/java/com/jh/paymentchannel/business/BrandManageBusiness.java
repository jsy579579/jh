package com.jh.paymentchannel.business;

import com.jh.paymentchannel.pojo.SwiftBrandMerchant;

public interface BrandManageBusiness {

	
	/**根据品牌获取用户的门店号*/
	public SwiftBrandMerchant  getSwiftBrandMerchant(String brandid);
	
	
	public SwiftBrandMerchant  getSwiftBrandMerchantByMchId(String merchant_id);
	
	
	public SwiftBrandMerchant  createSwiftBrandMerchant(SwiftBrandMerchant swiftBrandMerchant);

}
