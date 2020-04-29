package com.jh.paymentchannel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentchannel.pojo.YBHKBindCard;





@Repository
public interface YBHKBindCardRepository extends JpaRepository<YBHKBindCard, String>, JpaSpecificationExecutor<YBHKBindCard>{
	
	@Query("select cjhk from YBHKBindCard cjhk where cjhk.bankCard=:bankCard")
	public YBHKBindCard getYBHKBindCardByBankCard(@Param("bankCard") String bankCard);
	
}
