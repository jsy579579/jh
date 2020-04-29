package com.jh.paymentchannel.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentchannel.pojo.BankInfoCode;



@Repository
public interface BankInfoCodeRepository extends JpaRepository<BankInfoCode, String>, JpaSpecificationExecutor<BankInfoCode>{
	
	@Query("select bankInfoCode from BankInfoCode bankInfoCode where bankInfoCode.bankName=:bankName")
	public BankInfoCode getBankInfoCodeByBankName(@Param("bankName") String bankName);
	
	@Query("select bankInfoCode from BankInfoCode bankInfoCode where bankInfoCode.bankCode=:banknum and bankInfoCode.bankProvince=:bankprivince and bankInfoCode.bankCity=:bankcity")
	public List<BankInfoCode> getBankInfoCodeByThree(@Param("banknum") String banknum, @Param("bankprivince") String bankprivince, @Param("bankcity") String bankcity);
}
