package com.jh.paymentchannel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentchannel.pojo.BatchNo;
import com.jh.paymentchannel.pojo.BranchNo;

@Repository
public interface BatchNoRepository extends  JpaRepository<BatchNo,Integer>,JpaSpecificationExecutor<BatchNo>{
	BatchNo findByBindId(String BindId);
	
	@Query("select bb from BranchNo bb where bb.bankName =:bankName")
	public BranchNo getJFMBankNoByBankName(@Param("bankName") String bankName);
}
