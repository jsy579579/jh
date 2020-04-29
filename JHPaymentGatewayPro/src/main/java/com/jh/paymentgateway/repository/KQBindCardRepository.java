package com.jh.paymentgateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentgateway.pojo.KQBindCard;

@Repository
public interface KQBindCardRepository extends JpaRepository<KQBindCard, String>, JpaSpecificationExecutor<KQBindCard> {
	@Query("select kqb from KQBindCard kqb where kqb.bankCard=:bankCard")
	public KQBindCard getKQBindCardByBankCard(@Param("bankCard") String bankCard);
}
