package com.jh.paymentgateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentgateway.pojo.MHHQGBindCard;





@Repository
public interface MHHQGBindCardRepository extends JpaRepository<MHHQGBindCard, String>, JpaSpecificationExecutor<MHHQGBindCard>{
	
	@Query("select hqg from MHHQGBindCard hqg where hqg.bankCard=:bankCard")
	public MHHQGBindCard getMHHQGBindCardByBankCard(@Param("bankCard") String bankCard);
	
	@Query("select hqg from MHHQGBindCard hqg where hqg.merchantOrder=:merchantOrder")
	public MHHQGBindCard getMHHQGBindCardbyMerchantOrder(@Param("merchantOrder")String merchantOrder) ;
	
	
}
