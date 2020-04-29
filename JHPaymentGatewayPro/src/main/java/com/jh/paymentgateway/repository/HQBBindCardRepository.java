package com.jh.paymentgateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentgateway.pojo.HQBBindCard;


@Repository
public interface HQBBindCardRepository extends JpaRepository<HQBBindCard, String>, JpaSpecificationExecutor<HQBBindCard>{
	
	@Query("select hq from HQBBindCard hq where hq.bankCard=:bankCard")
	public HQBBindCard getHQBBindCardByBankCard(@Param("bankCard") String bankCard);

	public HQBBindCard getHQBBindCardByUserId(String userId);
	
}
