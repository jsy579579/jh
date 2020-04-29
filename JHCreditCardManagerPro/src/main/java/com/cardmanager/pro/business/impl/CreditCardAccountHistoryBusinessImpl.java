package com.cardmanager.pro.business.impl;

import java.math.BigDecimal;
import java.util.List;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cardmanager.pro.business.CreditCardAccountHistoryBusiness;
import com.cardmanager.pro.pojo.CreditCardAccountHistory;
import com.cardmanager.pro.repository.CreditCardAccountHistoryRepository;
@Service
public class CreditCardAccountHistoryBusinessImpl implements CreditCardAccountHistoryBusiness {

	@Autowired
	private CreditCardAccountHistoryRepository creditCardAccountHistoryRepository;
	
	@Autowired
	private EntityManager em;

	@Transactional
	@Override
	public CreditCardAccountHistory save(CreditCardAccountHistory creditCardAccountHistory) {
		creditCardAccountHistory = creditCardAccountHistoryRepository.saveAndFlush(creditCardAccountHistory);
		return creditCardAccountHistory;
	}

	@Transactional
	@Override
	public CreditCardAccountHistory createNewHistory(int addOrSub, BigDecimal amount, String taskId, Long creditCardAccountId,
			BigDecimal blance, String description) {
		CreditCardAccountHistory creditCardAccountHistory = new CreditCardAccountHistory();
		creditCardAccountHistory.setAddOrSub(addOrSub);
		creditCardAccountHistory.setAmount(amount.setScale(2, BigDecimal.ROUND_HALF_UP));
		creditCardAccountHistory.setTaskId(taskId);;
		creditCardAccountHistory.setCreditCardAccountId(creditCardAccountId);
		creditCardAccountHistory.setSumBlance(blance);
		creditCardAccountHistory.setDescription(description);
		return this.save(creditCardAccountHistory);
	}

	@Override
	public List<CreditCardAccountHistory> findByTaskId(String taskId) {
		List<CreditCardAccountHistory> findByTaskId = creditCardAccountHistoryRepository.findByTaskId(taskId);
		return findByTaskId;
	}

	@Override
	public CreditCardAccountHistory findByTaskIdAndAddOrSub(String taskId, int addOrSub) {
		CreditCardAccountHistory creditCardAccountHistory = creditCardAccountHistoryRepository.findByTaskIdAndAddOrSub(taskId,addOrSub);
		return creditCardAccountHistory;
	}

	@Override
	public List<CreditCardAccountHistory> findByCreditCardAccountIdAndAddOrSubOrderByCreateTimeDesc(
			Long creditCardAccountId, int addOrSub) {
		return creditCardAccountHistoryRepository.findByCreditCardAccountIdAndAddOrSubOrderByCreateTimeDesc(creditCardAccountId, addOrSub);
	}
}
