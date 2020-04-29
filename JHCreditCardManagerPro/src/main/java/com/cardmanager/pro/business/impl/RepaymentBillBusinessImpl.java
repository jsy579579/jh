package com.cardmanager.pro.business.impl;

import java.math.BigDecimal;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.cardmanager.pro.business.RepaymentBillBusiness;
import com.cardmanager.pro.pojo.RepaymentBill;
import com.cardmanager.pro.repository.RepaymentBillRepository;

@Service
public class RepaymentBillBusinessImpl implements RepaymentBillBusiness {

	@Autowired
	private RepaymentBillRepository repaymentBillRepository;
	
	@Override
	@Transactional
	public RepaymentBill save(RepaymentBill repaymentBill) {
		return repaymentBillRepository.saveAndFlush(repaymentBill);
	}

	@Override
	public RepaymentBill findByCreditCardNumberAndCreateTime(String creditCardNumber, String createTime) {
		return repaymentBillRepository.findByCreditCardNumberAndCreateTime(creditCardNumber, createTime);
	}

	@Override
	@Transactional
	public void delete(RepaymentBill repaymentBill) {
		repaymentBillRepository.delete(repaymentBill);
	}

	@Override
	public List<RepaymentBill> findByCreditCardNumberAndTaskStatusIn(String creditCardNumber, Integer[] taskStatus) {
		return repaymentBillRepository.findByCreditCardNumberAndTaskStatusIn(creditCardNumber, taskStatus);
	}

	@Override
	public Page<RepaymentBill> findByUserIdAndCreditCardNumber(String userId, String creditCardNumber,
			Pageable pageable) {
		return repaymentBillRepository.findByUserIdAndCreditCardNumber(userId, creditCardNumber,pageable);
	}

	@Override
	public Page<RepaymentBill> findByVersionAndLastExecuteDateTimeLessThanAndTaskStatusNot(String version,String lastExecuteTime, int taskStatus,Pageable pageable) {
		return repaymentBillRepository.findByVersionAndLastExecuteDateTimeLessThanAndTaskStatusNot(version, lastExecuteTime, taskStatus, pageable);
	}

	@Override
	public RepaymentBill findById(Long id) {
		return repaymentBillRepository.findOne(id);
	}

	@Override
	public RepaymentBill queryTaskAmountByCreateTime(String createTime) {
		return repaymentBillRepository.queryTaskAmountByCreateTime(createTime);
	}




}
