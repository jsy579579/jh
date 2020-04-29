package com.jh.notice.business.impl;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jh.notice.business.SMSBlackListBusiness;
import com.jh.notice.pojo.SMSBlackList;
import com.jh.notice.repository.SMSBlackListRepository;

@Service
public class SMSBlackListBusinessImpl implements SMSBlackListBusiness {

	@Autowired
	private SMSBlackListRepository smsBlackListRepository;
	
	@Autowired
	private EntityManager em;
	
	@Override
	public SMSBlackList findByIpAddress(String ipAddress) {
		return smsBlackListRepository.findByIpAddress(ipAddress);
	}

	@Transactional
	@Override
	public SMSBlackList save(SMSBlackList smsBlackList) {
		smsBlackList = smsBlackListRepository.save(smsBlackList);
		em.flush();
		em.clear();
		return smsBlackList;
	}

}
