package com.jh.paymentchannel.business.impl;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jh.paymentchannel.business.CreditCardInfoBusiness;
import com.jh.paymentchannel.pojo.CreditCardInfo;
import com.jh.paymentchannel.repository.CreditCardInfoRepository;

@Service
public class CreditCardInfoBusinessImpl implements CreditCardInfoBusiness{

	@Autowired
	private CreditCardInfoRepository creditCardInfoRepository;

	@Override
	public CreditCardInfo findByCardNo(String cardNo) {
		return creditCardInfoRepository.findByCardNo(cardNo);
	}

	@Transactional
	@Override
	public CreditCardInfo save(CreditCardInfo creditCardInfo) {
		return creditCardInfoRepository.saveAndFlush(creditCardInfo);
	}
}
