package com.jh.paymentchannel.business;

import com.jh.paymentchannel.pojo.CreditCardInfo;

public interface CreditCardInfoBusiness {

	CreditCardInfo findByCardNo(String cardNo);

	CreditCardInfo save(CreditCardInfo creditCardInfo);

}
