package com.jh.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.jh.user.pojo.CreditCoinExchangeBank;

@Repository
public interface CreditCoinExchangeBankRepository extends JpaRepository<CreditCoinExchangeBank, Integer>,JpaSpecificationExecutor<CreditCoinExchangeBank> {

	CreditCoinExchangeBank findByBankCode(String bankCode);

}
