package com.jh.paymentgateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentgateway.pojo.BQBankCard;
import com.jh.paymentgateway.pojo.BQRegister;

@Repository
public interface BQBankCardRepository extends JpaRepository<BQBankCard, Long>,JpaSpecificationExecutor<BQBankCard>{

	@Query("select bq from BQBankCard bq where bq.idNum=:idNum and bq.acctNo=:acctNo and status=1")
	public BQBankCard getBQRegisterByIdCard(@Param("idNum") String idNum,@Param("acctNo") String acct_no);
	
	@Query("select bq from BQBankCard bq where bq.idNum=:idNum and bq.acctNo=:acctNo and status=0")
	public BQBankCard getBQBankCardByIdNumSure(@Param("idNum") String idNum,@Param("acctNo") String acct_no);
	
	@Modifying
	@Query("update BQBankCard bq set bq.orgId=:orgId,bq.mchtId=:mchtId where bq.idNum=:idNum")
	void updateBQBankCard(@Param("orgId") String orgId,@Param("mchtId") String mchtId,@Param("idNum") String idNum);
	
}
