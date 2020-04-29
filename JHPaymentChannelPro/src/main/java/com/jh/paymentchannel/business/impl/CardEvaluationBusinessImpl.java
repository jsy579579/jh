package com.jh.paymentchannel.business.impl;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jh.paymentchannel.business.CardEvaluationBusiness;
import com.jh.paymentchannel.pojo.CardEvaInfo;
import com.jh.paymentchannel.pojo.UserQueryCount;
import com.jh.paymentchannel.repository.CardEvaInfoRepository;
import com.jh.paymentchannel.repository.UserQueryCountRepository;

@Service
public class CardEvaluationBusinessImpl implements CardEvaluationBusiness {

	
	@Autowired
	private EntityManager em;
	
	@Autowired
	private UserQueryCountRepository userQueryCountRepository;
	
	@Autowired
	private CardEvaInfoRepository cardEvaInfoRepository;
	
	@Transactional
	@Override
	public UserQueryCount createUserQueryCount(UserQueryCount userQueryCount) {
		UserQueryCount result = userQueryCountRepository.save(userQueryCount);
		em.flush();
		return result;
	}

	@Override
	public UserQueryCount getUserQueryCountByUserId(String userId) {
		em.clear();
		UserQueryCount result = userQueryCountRepository.getUserQueryCountByUserId(userId);
		return result;
	}

	@Transactional
	@Override
	public void createCardEvaInfo(CardEvaInfo cardEvaInfo) {
		cardEvaInfoRepository.save(cardEvaInfo);
		em.flush();
	}

	@Override
	public CardEvaInfo getCardEvaInfoByUserId(long userId) {
		em.clear();
		CardEvaInfo result = cardEvaInfoRepository.getCardEvaInfoByUserId(userId);
		return result;
	}

	
}
