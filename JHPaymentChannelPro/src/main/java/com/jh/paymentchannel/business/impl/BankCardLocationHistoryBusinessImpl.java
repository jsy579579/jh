package com.jh.paymentchannel.business.impl;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jh.paymentchannel.business.BankCardLocationHistoryBusiness;
import com.jh.paymentchannel.pojo.BankCardLocationHistory;
import com.jh.paymentchannel.repository.BankCardLocationHistoryRepository;

@Service
public class BankCardLocationHistoryBusinessImpl implements BankCardLocationHistoryBusiness {

	@Autowired
	private BankCardLocationHistoryRepository bankCardLocationHistoryRepository;
	
	@Autowired
	private EntityManager em;

	@Override
	@Transactional
	public BankCardLocationHistory save(BankCardLocationHistory bankCardLocationHistory) {
		BankCardLocationHistory cardLocationHistory = bankCardLocationHistoryRepository.save(bankCardLocationHistory);
		em.flush();
		em.clear();
		return cardLocationHistory;
	}
	
}
