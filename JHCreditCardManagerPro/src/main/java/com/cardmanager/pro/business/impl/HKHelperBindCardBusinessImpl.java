package com.cardmanager.pro.business.impl;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cardmanager.pro.business.HKHelperBindCardBusiness;
import com.cardmanager.pro.pojo.HKHelperBindCard;
import com.cardmanager.pro.repository.HKHelperBindCardRepository;

@Service
public class HKHelperBindCardBusinessImpl implements HKHelperBindCardBusiness {

	@Autowired
	private HKHelperBindCardRepository hkHelperBindCardRepository;

	@Override
	public List<HKHelperBindCard> findByUserId(String userId) {
		return hkHelperBindCardRepository.findByUserId(userId);
	}

	@Override
	public HKHelperBindCard findByUserIdAndCardNo(String userId, String creditCardNumber) {
		return hkHelperBindCardRepository.findByUserIdAndCardNo(userId, creditCardNumber);
	}

	@Override
	@Transactional
	public HKHelperBindCard saveNew(HKHelperBindCard hkHelperBindCard) {
		return hkHelperBindCardRepository.saveAndFlush(hkHelperBindCard);
	}
}
