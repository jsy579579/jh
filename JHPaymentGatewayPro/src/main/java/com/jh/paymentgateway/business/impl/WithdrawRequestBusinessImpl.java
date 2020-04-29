package com.jh.paymentgateway.business.impl;


import org.springframework.stereotype.Service;

import com.jh.paymentgateway.business.WithdrawRequestBusiness;

import java.math.BigDecimal;
@Service
public class WithdrawRequestBusinessImpl implements WithdrawRequestBusiness{

	@Override
	public BigDecimal CheckBalanceRequest()  {
		return BigDecimal.ZERO;
	}


}
