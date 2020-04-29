package com.jh.paymentgateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentgateway.pojo.HQBindCard;





@Repository
public interface HQBindCardRepository extends JpaRepository<HQBindCard, String>, JpaSpecificationExecutor<HQBindCard>{
	
	@Query("select hq from HQBindCard hq where hq.bankCard=:bankCard")
	public HQBindCard getHQBindCardByBankCard(@Param("bankCard") String bankCard);
	
	
}
