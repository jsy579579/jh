package com.jh.paymentgateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentgateway.pojo.HZHKBindCard;
import com.jh.paymentgateway.pojo.HZHKRegister;

@Repository
public interface HZHKBindCardRepository extends JpaRepository<HZHKBindCard, String>,JpaSpecificationExecutor<HZHKBindCard>{

	@Query("select hzhk from HZHKBindCard hzhk where hzhk.bankCard=:bankCard")
	public HZHKBindCard getHZHKBindCardByBankCard(@Param("bankCard") String bankCard);

}
