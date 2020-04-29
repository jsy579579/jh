package com.jh.paymentchannel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentchannel.pojo.WMYKBindCard;





@Repository
public interface WMYKBindCardRepository extends JpaRepository<WMYKBindCard, String>, JpaSpecificationExecutor<WMYKBindCard>{
	
	@Query("select wmyk from WMYKBindCard wmyk where wmyk.bankCard=:bankCard")
	public WMYKBindCard getWMYKBindCardByBankCard(@Param("bankCard") String bankCard);
	
	/*@Query("select hljc from HLJCBindCard hljc where hljc.orderCode=:orderCode")
	public HLJCBindCard getHLJCBindCardByOrderCode(@Param("orderCode") String orderCode);*/
	
}
