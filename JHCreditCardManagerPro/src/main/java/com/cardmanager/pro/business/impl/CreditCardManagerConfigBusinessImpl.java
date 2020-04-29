package com.cardmanager.pro.business.impl;

import java.util.List;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cardmanager.pro.business.CreditCardManagerConfigBusiness;
import com.cardmanager.pro.pojo.CreditCardManagerConfig;
import com.cardmanager.pro.repository.CreditCardManagerConfigRepository;

@Service
public class CreditCardManagerConfigBusinessImpl implements CreditCardManagerConfigBusiness {

	@Autowired
	private CreditCardManagerConfigRepository creditCardManagerConfigRepository;
	
	@Autowired
	private EntityManager em;

	@Transactional
	@Override
	public CreditCardManagerConfig save(CreditCardManagerConfig model) {
		model = creditCardManagerConfigRepository.saveAndFlush(model);
		return model;
	}

	@Override
	public CreditCardManagerConfig findByVersion(String version) {
		return creditCardManagerConfigRepository.findByVersion(version);
	}

	@Override
	public List<CreditCardManagerConfig> findAll() {
		return creditCardManagerConfigRepository.findAll();
	}

	@Override
	@Transactional
	public CreditCardManagerConfig findByVersionLock(String version) {
		CreditCardManagerConfig config = creditCardManagerConfigRepository.findByVersionLock(version);
		if (config.getScanOnOff() == 1) {
			config.setScanOnOff(0);
			this.save(config);
			return config;
		}
		return null;
	}

	@Override
	public List<CreditCardManagerConfig> findByCreateOnOff(int createOnOff) {
		return creditCardManagerConfigRepository.findByCreateOnOff(createOnOff);
	}

	@Override
	public CreditCardManagerConfig findCardManangerByVersion(String version) {
		return creditCardManagerConfigRepository.findCardManangerByVersion(version);
	}

}
