package com.jh.paymentchannel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentchannel.pojo.YLDZBindCard;





@Repository
public interface YLDZBindCardRepository extends JpaRepository<YLDZBindCard, String>, JpaSpecificationExecutor<YLDZBindCard>{
	
	@Query("select cjhk from YLDZBindCard cjhk where cjhk.bankCard=:bankCard")
	public YLDZBindCard getYLDZBindCardByBankCard(@Param("bankCard") String bankCard);
	
}
