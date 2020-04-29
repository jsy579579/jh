package com.jh.user.business;

import com.jh.user.pojo.BankCode;

public interface BankCodeBusiness {
	
	public String getCodeByName(String name);
	
	public BankCode getBankCode(String bankName);
	
}
