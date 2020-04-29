package com.cardmanager.pro.repository;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.cardmanager.pro.pojo.RepaymentBill;

@Repository
public interface RepaymentBillRepository extends JpaRepository<RepaymentBill, Long>, JpaSpecificationExecutor<RepaymentBill> {

	RepaymentBill findByCreditCardNumberAndCreateTime(String creditCardNumber, String createTime);

	List<RepaymentBill> findByCreditCardNumberAndTaskStatusIn(String creditCardNumber, Integer[] taskStatus);

	Page<RepaymentBill> findByUserIdAndCreditCardNumber(String userId, String creditCardNumber, Pageable pageable);

	Page<RepaymentBill> findByVersionAndLastExecuteDateTimeLessThanAndTaskStatusNot(String version,String lastExecuteTime, int taskStatus,Pageable pageable);

	@Query(value = "select a from RepaymentBill  a where a.createTime = :createTime")
    RepaymentBill queryTaskAmountByCreateTime(@Param("createTime")String createTime);



}
