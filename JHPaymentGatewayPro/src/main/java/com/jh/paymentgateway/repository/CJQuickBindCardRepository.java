package com.jh.paymentgateway.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentgateway.pojo.CJQuickBindCard;

@Repository
public interface CJQuickBindCardRepository extends JpaRepository<CJQuickBindCard, String>, JpaSpecificationExecutor<CJQuickBindCard>{
	
	@Query("select cjhk from CJQuickBindCard cjhk where cjhk.bankCard=:bankCard")
	public CJQuickBindCard getCJQuickBindCardByBankCard(@Param("bankCard") String bankCard);

	public List<CJQuickBindCard> findByIdCard(String idCard);
	
}
