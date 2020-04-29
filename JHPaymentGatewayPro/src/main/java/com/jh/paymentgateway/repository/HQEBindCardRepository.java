package com.jh.paymentgateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentgateway.pojo.HQEBindCard;





@Repository
public interface HQEBindCardRepository extends JpaRepository<HQEBindCard, String>, JpaSpecificationExecutor<HQEBindCard>{
	
	@Query("select hq from HQEBindCard hq where hq.bankCard=:bankCard")
	public HQEBindCard getHQEBindCardByBankCard(@Param("bankCard") String bankCard);
	
	@Query("select hq from HQEBindCard hq where hq.orderCode=:orderCode")
	public HQEBindCard getHQEBindCardByOrderCode(@Param("orderCode") String orderCode);
	
}
