package com.jh.paymentchannel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentchannel.pojo.WLBBindCard;



@Repository
public interface WLBBindCardRepository extends JpaRepository<WLBBindCard, String>, JpaSpecificationExecutor<WLBBindCard>{
	
	@Query("select wlb from WLBBindCard wlb where wlb.bankCard=:bankCard")
	public WLBBindCard getWLBBindCardBybankCard(@Param("bankCard") String bankCard);
	
}
