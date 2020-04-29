package com.jh.user.business.impl;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jh.user.business.BankCodeBusiness;
import com.jh.user.pojo.BankCode;
import com.jh.user.repository.BankCodeRepository;

@Service
public class BankCodeBusinessImpl implements BankCodeBusiness{
	
	
	@Autowired
	private BankCodeRepository bcRepository;
	
	@Autowired
	EntityManager em;
	
	@Override
	public String getCodeByName(String name) {
		return bcRepository.getCodeByName(name);
	}

	@Override
	public BankCode getBankCode(String bankName) {
		return bcRepository.getBankCode(bankName);
	}

}
