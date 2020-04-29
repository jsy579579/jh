package com.jh.paymentgateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentgateway.pojo.WFBindCard;





@Repository
public interface WFBindCardRepository extends JpaRepository<WFBindCard, String>, JpaSpecificationExecutor<WFBindCard>{
	
	@Query("select wf from WFBindCard wf where wf.bankCard=:bankCard")
	public WFBindCard getWFBindCardByBankCard(@Param("bankCard") String bankCard);
	
	
}
