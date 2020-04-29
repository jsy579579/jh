package com.jh.user.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.jh.user.pojo.CreditCoinExchangeOrder;

@Repository
public interface CreditCoinExchangeOrderRepository extends JpaRepository<CreditCoinExchangeOrder, Long>,JpaSpecificationExecutor<CreditCoinExchangeOrder>{

	CreditCoinExchangeOrder findByOrderCode(String orderCode);

	Page<CreditCoinExchangeOrder> findByOrderStatusAndExchangeTypeAndBrandId(Integer orderStatus,int exchangeType,String brandId,Pageable pageable);

	Page<CreditCoinExchangeOrder> findByUserIdAndExchangeTypeAndBrandId(String userId,int exchangeType,String brandId,Pageable pageable);

	Page<CreditCoinExchangeOrder> findByOrderStatusAndUserIdAndExchangeTypeAndBrandId(Integer orderStatus, String userId,int exchangeType,String brandId,Pageable pageable);

	Page<CreditCoinExchangeOrder> findByExchangeTypeAndBrandId(int exchangeType,String brandId,Pageable pageable);

	CreditCoinExchangeOrder findByOrderCodeAndExchangeType(String orderCode, int exchangeType);

	List<CreditCoinExchangeOrder> findListByOrderCodeAndExchangeType(String orderCode, int exchangeType);

}
