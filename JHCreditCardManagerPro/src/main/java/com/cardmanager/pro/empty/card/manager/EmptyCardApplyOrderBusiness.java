package com.cardmanager.pro.empty.card.manager;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EmptyCardApplyOrderBusiness {

	List<EmptyCardApplyOrder> findByCreditCardNumberAndOrderStatusIn(String creditCardNumber, int[] orderStatus);

	EmptyCardApplyOrder save(EmptyCardApplyOrder emptyCardApplyOrder);

	Page<EmptyCardApplyOrder> getAppalyOrder(String userId, String phone, String name, String creditCardNumber, String brandId,String status, String startTime, String endTime, Pageable pageable);

	EmptyCardApplyOrder findById(Long id);

	EmptyCardApplyOrder findByPaychargeOrderCode(String orderCode);

	void cancelAllTask(String userId,String creditCardNumber,String createTime,String version,BigDecimal debt);

	EmptyCardApplyOrder findByCreditCardNumberAndCreateTime(String creditCardNumber, Date createTime);

}
