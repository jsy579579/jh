package com.jh.paymentgateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentgateway.pojo.FFZCBindCard;
import com.jh.paymentgateway.pojo.FFZCRegister;

@Repository
public interface FFZCBindCardRepository extends JpaRepository<FFZCBindCard, String>, JpaSpecificationExecutor<FFZCBindCard>{

	@Query("select ffzc from FFZCBindCard ffzc where ffzc.bankCard=:bankCard")
	public FFZCBindCard getFFZCBindCardByBankCard(@Param("bankCard") String bankCard);
	
}
