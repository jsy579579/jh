package com.jh.paymentgateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentgateway.pojo.RHJFBindCard;


@Repository
public interface RHJFBindCardRepository extends JpaRepository<RHJFBindCard, String>, JpaSpecificationExecutor<RHJFBindCard>{
	
	@Query("select rhjf from RHJFBindCard rhjf where rhjf.bankCard=:bankCard and status=:status")
	public RHJFBindCard getRHJFBindCardByBankCard(@Param("bankCard") String bankCard,@Param("status") String status);
	
	
}
