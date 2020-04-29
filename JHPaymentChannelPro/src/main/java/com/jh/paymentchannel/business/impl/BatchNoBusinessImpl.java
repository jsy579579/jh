package com.jh.paymentchannel.business.impl;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jh.paymentchannel.business.BatchNoBusiness;
import com.jh.paymentchannel.pojo.BatchNo;
import com.jh.paymentchannel.repository.BatchNoRepository;
@Service
public class BatchNoBusinessImpl implements BatchNoBusiness {
	@Autowired
	private BatchNoRepository bnr;
	@Autowired
	private EntityManager em;
	@Override
	public BatchNo findByBindId(String BindId) {
		// TODO Auto-generated method stub
		return bnr.findByBindId(BindId);
	}
	@Override
	@Transactional
	public BatchNo save(BatchNo batchno) {
		// TODO Auto-generated method stub
		BatchNo batch = bnr.save(batchno);
		em.flush();
		em.clear();
		return batch;
	}

}
