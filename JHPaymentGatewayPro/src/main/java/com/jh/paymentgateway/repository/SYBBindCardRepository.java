package com.jh.paymentgateway.repository;

import com.jh.paymentgateway.pojo.SYBBindCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface SYBBindCardRepository extends JpaRepository<SYBBindCard, String>,JpaSpecificationExecutor<SYBBindCard>{
	
	@Query("select syb from SYBBindCard syb where syb.bankCard=?1")
	public SYBBindCard getSYBBindCardByBankCard(@Param("bankCard") String bankCard);

}
