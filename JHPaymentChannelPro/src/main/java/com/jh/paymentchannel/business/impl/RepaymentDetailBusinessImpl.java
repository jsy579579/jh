package com.jh.paymentchannel.business.impl;

import java.util.List;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jh.paymentchannel.business.RepaymentDetailBusiness;
import com.jh.paymentchannel.pojo.RepaymentDetail;
import com.jh.paymentchannel.repository.RepaymentDetailRepository;

@Service
public class RepaymentDetailBusinessImpl implements RepaymentDetailBusiness {

	
	@Autowired
	private EntityManager em;

	@Autowired
	private RepaymentDetailRepository repaymentDetailRepository;

	@Override
	public List<RepaymentDetail> getRepaymentDetailAll() {
		em.clear();
		List<RepaymentDetail> result = repaymentDetailRepository.getRepaymentDetailAll();
		return result;
	}

	@Override
	public List<RepaymentDetail> findByVersionIn(String[] versions) {
		return repaymentDetailRepository.findByVersionInOrderBySortAsc(versions);
	}
	
	
	
	
	
	
}
