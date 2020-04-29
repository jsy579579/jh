package com.cardmanager.pro.empty.card.manager;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface EmptyCardApplyOrderRepository extends JpaRepository<EmptyCardApplyOrder, Long>, JpaSpecificationExecutor<EmptyCardApplyOrder> {

	List<EmptyCardApplyOrder> findByCreditCardNumberAndOrderStatusIn(String creditCardNumber, int[] orderStatus);

	EmptyCardApplyOrder findByPaychargeOrderCode(String orderCode);

	EmptyCardApplyOrder findByCreditCardNumberAndCreateTime(String creditCardNumber, Date createTime);

	EmptyCardApplyOrder findByCreditCardNumberAndCreateTime(String creditCardNumber, String createTime);

}
