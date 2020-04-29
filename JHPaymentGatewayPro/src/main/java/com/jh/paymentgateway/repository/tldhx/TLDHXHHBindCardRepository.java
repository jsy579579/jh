package com.jh.paymentgateway.repository.tldhx;

import com.jh.paymentgateway.pojo.tldhx.TLDHXHHBindCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TLDHXHHBindCardRepository extends JpaRepository<TLDHXHHBindCard, String>, JpaSpecificationExecutor<TLDHXHHBindCard> {
	@Query("select tl from TLDHXHHBindCard tl where tl.bankCard=:bankCard")
	public TLDHXHHBindCard getTLDHXBindCardByBankCard(@Param("bankCard") String bankCard);
}
