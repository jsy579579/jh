package com.jh.paymentgateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.paymentgateway.pojo.KYBindCard;
@Repository
public interface KYBindCardRepository extends JpaRepository<KYBindCard, String>, JpaSpecificationExecutor<KYBindCard> {
	@Query("select ky from KYBindCard ky where ky.bankCard=:bankCard")
	public KYBindCard getKYBindCardBybankCard(@Param("bankCard") String bankCard);
}
