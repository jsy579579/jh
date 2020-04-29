package com.jh.paymentchannel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.jh.paymentchannel.pojo.BranchNo;

@Repository
public interface BranchNoRepository extends  JpaRepository<BranchNo,Integer>,JpaSpecificationExecutor<BranchNo>{

	BranchNo findBankNoByBankName(String bankName);

}
