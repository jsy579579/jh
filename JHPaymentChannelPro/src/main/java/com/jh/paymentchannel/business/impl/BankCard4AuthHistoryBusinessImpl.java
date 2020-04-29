package com.jh.paymentchannel.business.impl;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jh.paymentchannel.business.BankCard4AuthHistoryBusiness;
import com.jh.paymentchannel.pojo.BankCard4AuthHistory;
import com.jh.paymentchannel.repository.BankCard4AuthHistoryRepository;

@Service
public class BankCard4AuthHistoryBusinessImpl implements BankCard4AuthHistoryBusiness {
	
	@Autowired
	private BankCard4AuthHistoryRepository bankCard4AuthHistoryRepository;

	@Override
	@Transactional
	public BankCard4AuthHistory save(BankCard4AuthHistory bankCard4AuthHistory) {
		return bankCard4AuthHistoryRepository.save(bankCard4AuthHistory);
	}

}
