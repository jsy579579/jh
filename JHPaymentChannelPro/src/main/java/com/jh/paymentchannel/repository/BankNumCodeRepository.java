package com.jh.paymentchannel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentchannel.pojo.BankNumCode;



@Repository
public interface BankNumCodeRepository extends JpaRepository<BankNumCode, String>, JpaSpecificationExecutor<BankNumCode>{
	
	@Query("select bankNumCode from BankNumCode bankNumCode where bankNumCode.bankName=:bankName")
	public BankNumCode getBankNumCodeByBankName(@Param("bankName") String bankName);
	
}
