package com.jh.paymentgateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentgateway.pojo.JFBindCard;

@Repository
public interface JFBindCardRepository extends JpaRepository<JFBindCard, String>, JpaSpecificationExecutor<JFBindCard> {
	@Query("select jf from JFBindCard jf where jf.bankCard=:bankCard")
	public JFBindCard getJFBindCardByBankCard(@Param("bankCard") String bankCard);
}
