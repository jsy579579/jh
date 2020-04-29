package com.jh.paymentgateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentgateway.pojo.HQHBindCard;





@Repository
public interface HQHBindCardRepository extends JpaRepository<HQHBindCard, String>, JpaSpecificationExecutor<HQHBindCard>{
	
	@Query("select hqh from HQHBindCard hqh where hqh.bankCard=:bankCard")
	public HQHBindCard getHQHBindCardByBankCard(@Param("bankCard") String bankCard);
	
	@Query("select hqh from HQHBindCard hqh where hqh.merchantOrder=:merchantOrder")
	public HQHBindCard getHQHBindCardbyMerchantOrder(@Param("merchantOrder")String merchantOrder);
	
}
