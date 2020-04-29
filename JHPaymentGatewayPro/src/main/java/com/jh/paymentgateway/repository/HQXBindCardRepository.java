package com.jh.paymentgateway.repository;

import com.jh.paymentgateway.pojo.HQXBindCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface HQXBindCardRepository extends JpaRepository<HQXBindCard, String>, JpaSpecificationExecutor<HQXBindCard> {

    @Query("select hqx from HQXBindCard hqx where hqx.bankCard=:bankCard")
    HQXBindCard getHQXBindCardByBankCard(@Param("bankCard") String bankCard);

    @Query("select hqx from HQXBindCard hqx where hqx.orderId=:orderId")
    HQXBindCard getHQXBindCardByOrderId(@Param("orderId") String orderId);
}
