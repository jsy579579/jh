package com.jh.paymentgateway.business.impl;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jh.paymentgateway.business.OrderParameterBusiness;
import com.jh.paymentgateway.pojo.OrderParameter;
import com.jh.paymentgateway.repository.OrderParameterRepository;

@Service
public class OrderParameterBusinessImpl implements OrderParameterBusiness {

	@Autowired
	private OrderParameterRepository orderParameterRepository;
	
	@Override
	@Transactional
	public void save(OrderParameter orderParameter) {
		orderParameterRepository.saveAndFlush(orderParameter);
	}

	@Override
	public OrderParameter findByOrderCode(String orderCode) {
		return orderParameterRepository.findByOrderCode(orderCode);
	}

}
