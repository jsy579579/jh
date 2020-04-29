package com.jh.paymentgateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentgateway.pojo.LMBankNum;


@Repository
public interface LMBankNumRepository extends JpaRepository<LMBankNum, String>, JpaSpecificationExecutor<LMBankNum>{
	
	@Query("select lmBankNum from LMBankNum lmBankNum where lmBankNum.bankName=:bankName")
	public LMBankNum getLMBankNumCodeByBankName(@Param("bankName") String bankName);
	
}
