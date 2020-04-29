package com.jh.paymentgateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentgateway.pojo.JFXBindCard;

@Repository
public interface JFXBindCardRepository extends JpaRepository<JFXBindCard, String>, JpaSpecificationExecutor<JFXBindCard> {
	@Query("select jfx from JFXBindCard jfx where jfx.bankCard=:bankCard")
	public JFXBindCard getJFXBindCardByBankCard(@Param("bankCard") String bankCard);
}
