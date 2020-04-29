package com.jh.paymentgateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentgateway.pojo.MHHQEBindCard;





@Repository
public interface MHHQEBindCardRepository extends JpaRepository<MHHQEBindCard, String>, JpaSpecificationExecutor<MHHQEBindCard>{
	
	@Query("select hq from MHHQEBindCard hq where hq.bankCard=:bankCard")
	public MHHQEBindCard getMHHQEBindCardByBankCard(@Param("bankCard") String bankCard);
	
	@Query("select hq from MHHQEBindCard hq where hq.orderCode=:orderCode")
	public MHHQEBindCard getMHHQEBindCardByOrderCode(@Param("orderCode") String orderCode);
	
}
