package com.jh.user.business;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.jh.user.pojo.CreditCoinExchangeOrder;

public interface CreditCoinExchangeOrderBusiness {

	CreditCoinExchangeOrder save(CreditCoinExchangeOrder creditCoinExchangeOrder);

	CreditCoinExchangeOrder findByOrderCodeAndExchangeType(String orderCode,int exchangeType);

	Page<CreditCoinExchangeOrder> findByOrderStatusAndExchangeTypeAndBrandId(String orderStatus,int exchangeType,String brandId,Pageable pageable);

	Page<CreditCoinExchangeOrder> findByOrderStatusAndUserIdAndExchangeTypeAndBrandId(String orderStatus, String userId,int exchangeType,String brandId,Pageable pageable);

	CreditCoinExchangeOrder findById(String orderId);

	List<CreditCoinExchangeOrder> findListByOrderCodeAndExchangeType(String orderCode, int exchangeType);

}
