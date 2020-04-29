package com.jh.user.business;

import com.jh.user.pojo.CreditCoinExchangeBank;

public interface CreditCoinExchangeBankBusiness {

	CreditCoinExchangeBank findByBankCode(String bankCode);

}
