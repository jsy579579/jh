package com.jh.paymentgateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentgateway.pojo.CJBindCard;

@Repository
public interface CJBindCardRepository extends JpaRepository<CJBindCard, String>, JpaSpecificationExecutor<CJBindCard>{
	
	@Query("select cjhk from CJBindCard cjhk where cjhk.bankCard=:bankCard")
	public CJBindCard getCJBindCardByBankCard(@Param("bankCard") String bankCard);
	
}
