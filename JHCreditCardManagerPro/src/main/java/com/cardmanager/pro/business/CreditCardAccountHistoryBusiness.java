package com.cardmanager.pro.business;

import java.math.BigDecimal;
import java.util.List;

import com.cardmanager.pro.pojo.CreditCardAccountHistory;

public interface CreditCardAccountHistoryBusiness {

	CreditCardAccountHistory save(CreditCardAccountHistory creditCardAccountHistory);

	CreditCardAccountHistory createNewHistory(int addOrSub, BigDecimal amount, String taskId, Long creditCardAccountId, BigDecimal blance,String description);

	List<CreditCardAccountHistory> findByTaskId(String taskId);

	CreditCardAccountHistory findByTaskIdAndAddOrSub(String taskId, int addOrSub);

	List<CreditCardAccountHistory> findByCreditCardAccountIdAndAddOrSubOrderByCreateTimeDesc(Long creditCardAccountId, int addOrSub);

}
