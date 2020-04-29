package com.jh.user.business.impl;

import java.util.List;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.jh.user.business.CreditCoinExchangeOrderBusiness;
import com.jh.user.pojo.CreditCoinExchangeOrder;
import com.jh.user.repository.CreditCoinExchangeOrderRepository;

@Service
public class CreditCoinExchangeOrderBusinessImpl implements CreditCoinExchangeOrderBusiness {

	@Autowired
	private CreditCoinExchangeOrderRepository creditCoinExchangeOrderRepository;
	
	@Autowired
	private EntityManager em;

	@Transactional
	@Override
	public CreditCoinExchangeOrder save(CreditCoinExchangeOrder creditCoinExchange) {
		return creditCoinExchangeOrderRepository.saveAndFlush(creditCoinExchange);
	}

	@Override
	public CreditCoinExchangeOrder findByOrderCodeAndExchangeType(String orderCode,int exchangeType) {
		em.clear();
		return creditCoinExchangeOrderRepository.findByOrderCodeAndExchangeType(orderCode,exchangeType);
	}

	@Override
	public Page<CreditCoinExchangeOrder> findByOrderStatusAndExchangeTypeAndBrandId(String orderStatus,int exchangeType,String brandId,Pageable pageable) {
		em.clear();
		if ("3".equals(orderStatus)) {
			return creditCoinExchangeOrderRepository.findByExchangeTypeAndBrandId(exchangeType,brandId,pageable);
		}
		return creditCoinExchangeOrderRepository.findByOrderStatusAndExchangeTypeAndBrandId(Integer.valueOf(orderStatus),exchangeType,brandId,pageable);
	}

	@Override
	public Page<CreditCoinExchangeOrder> findByOrderStatusAndUserIdAndExchangeTypeAndBrandId(String orderStatus, String userId,int exchangeType,String brandId,Pageable pageable) {
		em.clear();
		if ("3".equals(orderStatus)) {
			return creditCoinExchangeOrderRepository.findByUserIdAndExchangeTypeAndBrandId(userId,exchangeType,brandId,pageable);
		}
		return creditCoinExchangeOrderRepository.findByOrderStatusAndUserIdAndExchangeTypeAndBrandId(Integer.valueOf(orderStatus),userId,exchangeType,brandId,pageable);
	}

	@Override
	public CreditCoinExchangeOrder findById(String orderId) {
		em.clear();
		return creditCoinExchangeOrderRepository.findOne(Long.valueOf(orderId));
	}

	@Override
	public List<CreditCoinExchangeOrder> findListByOrderCodeAndExchangeType(String orderCode, int exchangeType) {
		em.clear();
		return creditCoinExchangeOrderRepository.findListByOrderCodeAndExchangeType(orderCode,exchangeType);
	}
}
