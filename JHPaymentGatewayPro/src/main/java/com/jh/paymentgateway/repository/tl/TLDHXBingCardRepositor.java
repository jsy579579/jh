package com.jh.paymentgateway.repository.tl;

import com.jh.paymentgateway.pojo.tl.TLDHXBindCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TLDHXBingCardRepositor extends JpaRepository<TLDHXBindCard,String>, JpaSpecificationExecutor<TLDHXBindCard> {
    @Query("select hxdhx from TLDHXBindCard hxdhx where hxdhx.bankCard=:bankCard")
    TLDHXBindCard getTLDHXBindCardByBankCard(@Param("bankCard") String bankCard);
}
