package com.cardmanager.pro.business;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.cardmanager.pro.pojo.RepaymentBill;

public interface RepaymentBillBusiness {

	RepaymentBill save(RepaymentBill repaymentBill);

	RepaymentBill findByCreditCardNumberAndCreateTime(String creditCardNumber, String createTime);

	void delete(RepaymentBill repaymentBill);

	List<RepaymentBill> findByCreditCardNumberAndTaskStatusIn(String creditCardNumber, Integer[] taskStatus);

	Page<RepaymentBill> findByUserIdAndCreditCardNumber(String userId, String creditCardNumber, Pageable pageable);

	Page<RepaymentBill> findByVersionAndLastExecuteDateTimeLessThanAndTaskStatusNot(String version,String lastExecuteTime, int taskStatus, Pageable pageable);

	RepaymentBill findById(Long id);

    RepaymentBill queryTaskAmountByCreateTime(String createTime);


}
