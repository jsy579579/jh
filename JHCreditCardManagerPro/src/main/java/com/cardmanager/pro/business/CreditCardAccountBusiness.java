package com.cardmanager.pro.business;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.cardmanager.pro.pojo.ConsumeTaskPOJO;
import com.cardmanager.pro.pojo.CreditCardAccount;

public interface CreditCardAccountBusiness {

	CreditCardAccount findByUserIdAndCreditCardNumberAndVersion(String userId, String creditCardNumber,String version);

	CreditCardAccount findByUserIdAndCreditCardNumberAndVersionLock(String userId, String creditCardNumber,String version);
	
	CreditCardAccount save(CreditCardAccount creditCardAccount);

	CreditCardAccount updateCreditCardAccountAndVersion(String userId, String creditCardNumber,String taskId, int addOrSub, BigDecimal amount,String description,String version,String billNo);

	List<CreditCardAccount> findCreditCardAccountByBlanceNotZeroAndVersion(BigDecimal firstAmount,String version,Pageable pageable);

	List<CreditCardAccount> findByFreezeBlanceGreaterThan0AndVersion(String version);

	CreditCardAccount findByCreditCardNumberAndVersion(String creditCardNumber,String version);

	List<CreditCardAccount> findByFreezeBlanceAndVersion(BigDecimal amount,String version);

	List<CreditCardAccount> findCreditCardAccountByBlanceLessTenAndVersion(String version,Pageable pageable);

	List<CreditCardAccount> findByBlanceAndVersionAndLastUpdateTimeLessThan(BigDecimal blance, String version,Date time);

	CreditCardAccount createNewAccount(String userId, String creditCardNumber, String version, String phone,Integer billDate, Integer repaymentDay, BigDecimal creditBlance,String brandId);

	CreditCardAccount createNewAccountAndFirstConsume(String userId, String creditCardNumber, String version,String phone, Integer billDate, Integer repaymentDate, BigDecimal creditCardBlance, ConsumeTaskPOJO consumeTaskPOJO,String brandId);

	List<CreditCardAccount> findByCreditCardNumber(String creditCardNumber);

	List<CreditCardAccount> findByPhone(String phone);

    CreditCardAccount getChangeInfo(String bankCard);

	CreditCardAccount getCreditCardAccount(String version, String creditCardNumber);

	CreditCardAccount updateCreditCardAccount(CreditCardAccount cardAccount);

	List<CreditCardAccount> findAll(CreditCardAccount cardAccount);
}
