package com.jh.paymentchannel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentchannel.pojo.CJHKBindCard;





@Repository
public interface CJHKBindCardRepository extends JpaRepository<CJHKBindCard, String>, JpaSpecificationExecutor<CJHKBindCard>{
	
	@Query("select cjhk from CJHKBindCard cjhk where cjhk.bankCard=:bankCard")
	public CJHKBindCard getCJHKBindCardByBankCard(@Param("bankCard") String bankCard);
	
	@Query("select cjhk from CJHKBindCard cjhk where cjhk.orderCode=:orderCode")
	public CJHKBindCard getCJHKBindCardByOrderCode(@Param("orderCode") String orderCode);
	
}
