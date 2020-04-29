package com.jh.paymentgateway.repository;

import com.jh.paymentgateway.pojo.XTBindCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface XTBindCardRepository extends JpaRepository<XTBindCard, String>, JpaSpecificationExecutor<XTBindCard> {
	@Query("select xt from XTBindCard xt where xt.bankCard=:bankCard")
	public XTBindCard getXTBindCardByBankCard(@Param("bankCard") String bankCard);
}
