package com.jh.paymentgateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentgateway.pojo.MHHQBindCard;





@Repository
public interface MHHQBindCardRepository extends JpaRepository<MHHQBindCard, String>, JpaSpecificationExecutor<MHHQBindCard>{
	
	@Query("select hq from MHHQBindCard hq where hq.bankCard=:bankCard")
	public MHHQBindCard getMHHQBindCardByBankCard(@Param("bankCard") String bankCard);
	
	
}
