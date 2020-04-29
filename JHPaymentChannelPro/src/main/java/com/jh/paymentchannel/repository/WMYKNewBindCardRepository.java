package com.jh.paymentchannel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentchannel.pojo.WMYKNewBindCard;





@Repository
public interface WMYKNewBindCardRepository extends JpaRepository<WMYKNewBindCard, String>, JpaSpecificationExecutor<WMYKNewBindCard>{
	
	@Query("select wmyk from WMYKNewBindCard wmyk where wmyk.bankCard=:bankCard")
	public WMYKNewBindCard getWMYKNewBindCardByBankCard(@Param("bankCard") String bankCard);
	
	
}
