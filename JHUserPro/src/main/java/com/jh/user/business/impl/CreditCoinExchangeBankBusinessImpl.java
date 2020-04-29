package com.jh.user.business.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jh.user.business.CreditCoinExchangeBankBusiness;
import com.jh.user.pojo.CreditCoinExchangeBank;
import com.jh.user.repository.CreditCoinExchangeBankRepository;

@Service
public class CreditCoinExchangeBankBusinessImpl implements CreditCoinExchangeBankBusiness {
	
	@Autowired
	private CreditCoinExchangeBankRepository creditCoinExchangeBankRepository;

	@Override
	public CreditCoinExchangeBank findByBankCode(String bankCode) {
		return creditCoinExchangeBankRepository.findByBankCode(bankCode);
	}
}
