package com.jh.channel.business;

import com.jh.channel.pojo.OutMerchantSecurityKey;

public interface OutMerchantSecurityBusiness {

	
	public  OutMerchantSecurityKey  getOutMerchantSecurityKey(String merchantcode);
	
	public  OutMerchantSecurityKey  addOutMerchantSecurityKey(OutMerchantSecurityKey outMerchantSecurityKey);
	
	public  OutMerchantSecurityKey  findOutMerchantSecurityKeyByUid(long uid);
	
	public  OutMerchantSecurityKey  getOutMerchantSecurityKeyid(String merchantid);
	
	
}
