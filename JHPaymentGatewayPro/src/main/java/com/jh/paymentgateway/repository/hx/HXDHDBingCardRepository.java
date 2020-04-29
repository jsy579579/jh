package com.jh.paymentgateway.repository.hx;

import com.jh.paymentgateway.pojo.hxdhd.HXDHDBindCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface HXDHDBingCardRepository extends JpaRepository<HXDHDBindCard,Integer>, JpaSpecificationExecutor<HXDHDBindCard> {

    @Query("select hxdhd from HXDHDBindCard hxdhd where hxdhd.bankCard=:bankCard")
    HXDHDBindCard getHXDHDBindCardByBankCard(@Param("bankCard") String bankCard);
}
