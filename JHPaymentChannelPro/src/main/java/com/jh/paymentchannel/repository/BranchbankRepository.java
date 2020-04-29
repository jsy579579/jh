package com.jh.paymentchannel.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentchannel.pojo.BranchNo;
import com.jh.paymentchannel.pojo.Branchbank;
@Repository
public interface BranchbankRepository extends JpaRepository<Branchbank,String>,JpaSpecificationExecutor<Branchbank>{ 

	
	@Query("select branchbank from  Branchbank branchbank where branchbank.province like %:province% and branchbank.city like %:city% and branchbank.bankBranchname like %:bankBranchname%")
	public List<Branchbank>	queryInfoBranch(@Param("province") String province,@Param("city") String city,@Param("bankBranchname") String bankBranchname);
	
	
	@Query("select bb.bankBranchno from Branchbank bb where bb.bankBranchname = :bankBranchname")
	public String getNumByName(@Param("bankBranchname") String bankBranchname);
	
}