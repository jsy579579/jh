package com.jh.paymentgateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentgateway.pojo.HQGBindCard;





@Repository
public interface HQGBindCardRepository extends JpaRepository<HQGBindCard, String>, JpaSpecificationExecutor<HQGBindCard>{
	
	@Query("select hqg from HQGBindCard hqg where hqg.bankCard=:bankCard")
	public HQGBindCard getHQGBindCardByBankCard(@Param("bankCard") String bankCard);
	
	@Query("select hqg from HQGBindCard hqg where hqg.merchantOrder=:merchantOrder")
	public HQGBindCard getHQGBindCardbyMerchantOrder(@Param("merchantOrder")String merchantOrder) ;
	
	
}
