package com.jh.paymentgateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.jh.paymentgateway.pojo.RYTBindCard;


public interface RYTBindCardRepository extends JpaRepository<RYTBindCard, String>,JpaSpecificationExecutor<RYTBindCard>{
	
	@Query("select ryt from RYTBindCard ryt where ryt.bankCard=:bankCard")
	public RYTBindCard getRYTBindCardByBankCard(@Param("bankCard") String bankCard);

}
