package com.jh.paymentgateway.controller.hqm.repository;


import com.jh.paymentgateway.controller.hqm.pojo.HQMbindCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface HQMbindCardRepository extends JpaRepository<HQMbindCard,Long>, JpaSpecificationExecutor<HQMbindCard> {
    @Query("select hqt from HQMbindCard hqt where hqt.bankCard=:bankCard")
    HQMbindCard getHQMbindCardByBankCard(@Param("bankCard") String bankCard);

    @Query("select hqx from HQMbindCard hqx where hqx.orderId=:orderId")
    HQMbindCard getHQMBindCardByOrderId(@Param("orderId") String orderId);

}
