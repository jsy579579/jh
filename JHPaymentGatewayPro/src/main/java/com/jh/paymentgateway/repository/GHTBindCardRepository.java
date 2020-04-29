package com.jh.paymentgateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentgateway.pojo.GHTBindCard;





@Repository
public interface GHTBindCardRepository extends JpaRepository<GHTBindCard, String>, JpaSpecificationExecutor<GHTBindCard>{
	
	@Query("select ght from GHTBindCard ght where ght.bankCard=:bankCard")
	public GHTBindCard getGHTBindCardByBankCard(@Param("bankCard") String bankCard);
	
	@Query("select ght from GHTBindCard ght where ght.orderCode=:orderCode")
	public GHTBindCard getGHTBindCardByOrderCode(@Param("orderCode") String orderCode);
	
}
