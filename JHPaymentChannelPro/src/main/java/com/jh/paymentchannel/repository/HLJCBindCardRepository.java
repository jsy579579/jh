package com.jh.paymentchannel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentchannel.pojo.HLJCBindCard;





@Repository
public interface HLJCBindCardRepository extends JpaRepository<HLJCBindCard, String>, JpaSpecificationExecutor<HLJCBindCard>{
	
	@Query("select hljc from HLJCBindCard hljc where hljc.bankCard=:bankCard")
	public HLJCBindCard getHLJCBindCardByBankCard(@Param("bankCard") String bankCard);
	
	@Query("select hljc from HLJCBindCard hljc where hljc.orderCode=:orderCode")
	public HLJCBindCard getHLJCBindCardByOrderCode(@Param("orderCode") String orderCode);
	
}
