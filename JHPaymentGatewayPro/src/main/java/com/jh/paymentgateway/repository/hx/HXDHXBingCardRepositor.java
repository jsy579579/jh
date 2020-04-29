package com.jh.paymentgateway.repository.hx;

import com.jh.paymentgateway.pojo.hx.HXDHXBindCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HXDHXBingCardRepositor extends JpaRepository<HXDHXBindCard,String >, JpaSpecificationExecutor<HXDHXBindCard> {

    @Query("select hxdhx from HXDHXBindCard hxdhx where hxdhx.bankCard=:bankCard")
    public HXDHXBindCard getHQXBindCardByBankCard(@Param("bankCard") String bankCard);
}
