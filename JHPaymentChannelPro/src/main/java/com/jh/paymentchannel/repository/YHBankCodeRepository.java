package com.jh.paymentchannel.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentchannel.pojo.YHBankCode;


@Repository
public interface YHBankCodeRepository extends JpaRepository<YHBankCode, String>, JpaSpecificationExecutor<YHBankCode>{
	
	@Query("select yh.bankCode from YHBankCode yh where yh.bankName=:bankName")
	public String getBankCodeByBankName(@Param("bankName") String bankName );
	
	
}
