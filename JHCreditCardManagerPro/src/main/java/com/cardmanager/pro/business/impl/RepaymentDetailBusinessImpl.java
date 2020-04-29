package com.cardmanager.pro.business.impl;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cardmanager.pro.business.RepaymentDetailBusiness;
import com.cardmanager.pro.pojo.RepaymentBrandStatus;
import com.cardmanager.pro.repository.RepaymentBrandStatusRepository;

@Service
public class RepaymentDetailBusinessImpl implements RepaymentDetailBusiness {

	
	@Autowired
	private EntityManager em;

	@Autowired
	private RepaymentBrandStatusRepository repaymentBrandStatusRepository;
	
	@Transactional
	@Override
	public void createRepaymentBrandStatus(RepaymentBrandStatus repaymentBrandStatus) {
		repaymentBrandStatusRepository.saveAndFlush(repaymentBrandStatus);
		em.clear();
	}

	@Override
	public RepaymentBrandStatus getRepaymentBrandStatusByBrandIdAndVersion(int brandId, String version) {
		em.clear();
		RepaymentBrandStatus result = repaymentBrandStatusRepository.getRepaymentBrandStatusByBrandIdAndVersion(brandId, version);
		return result;
	}

}
