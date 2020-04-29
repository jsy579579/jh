package com.jh.paymentgateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.jh.paymentgateway.pojo.MHHQHBindCard;





@Repository
public interface MHHQHBindCardRepository extends JpaRepository<MHHQHBindCard, String>, JpaSpecificationExecutor<MHHQHBindCard>{

	MHHQHBindCard getMHHQHBindCardByBankCard(String bankCard);
	
	
}
