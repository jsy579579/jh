package com.jh.paymentgateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentgateway.pojo.SSBindCard;


@Repository
public interface SSBindCardRepository extends JpaRepository<SSBindCard, String>, JpaSpecificationExecutor<SSBindCard>{
	
	@Query("select ss from SSBindCard ss where ss.bankCard=:bankCard")
	public SSBindCard getSSBindCardByBankCard(@Param("bankCard") String bankCard);
	
	@Query("select ss from SSBindCard ss where ss.bindId=:bindId")
	public SSBindCard getSSBindCardByBindId(@Param("bindId") String bindId);
	
}
