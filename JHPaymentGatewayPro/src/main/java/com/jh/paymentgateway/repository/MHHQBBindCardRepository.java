package com.jh.paymentgateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentgateway.pojo.MHHQBBindCard;


@Repository
public interface MHHQBBindCardRepository extends JpaRepository<MHHQBBindCard, String>, JpaSpecificationExecutor<MHHQBBindCard>{
	
	@Query("select hq from MHHQBBindCard hq where hq.bankCard=:bankCard")
	public MHHQBBindCard getMHHQBBindCardByBankCard(@Param("bankCard") String bankCard);

	public MHHQBBindCard getMHHQBBindCardByUserId(String userId);
	
}
