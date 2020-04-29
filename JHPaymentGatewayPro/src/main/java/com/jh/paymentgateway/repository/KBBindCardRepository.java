package com.jh.paymentgateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentgateway.pojo.KBBindCard;

@Repository
public interface KBBindCardRepository extends JpaRepository<KBBindCard, String>, JpaSpecificationExecutor<KBBindCard>{
	
	@Query("select kb from KBBindCard kb where kb.bankCard=:bankCard")
	public KBBindCard getKBBindCardByIdCard(@Param("bankCard") String bankCard);
	
}
