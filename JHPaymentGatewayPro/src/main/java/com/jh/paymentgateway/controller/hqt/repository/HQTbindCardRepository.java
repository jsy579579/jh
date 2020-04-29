package com.jh.paymentgateway.controller.hqt.repository;


import com.jh.paymentgateway.controller.hqt.pojo.HQTbindCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface HQTbindCardRepository extends JpaRepository<HQTbindCard,Long>, JpaSpecificationExecutor<HQTbindCard> {
    @Query("select hqt from HQTbindCard hqt where hqt.bankCard=:bankCard")
    HQTbindCard getHQTbindCardByBankCard(@Param("bankCard")String bankCard);

    @Query("select hqx from HQTbindCard hqx where hqx.orderId=:orderId")
    HQTbindCard getHQXBindCardByOrderId(@Param("orderId") String orderId);

}
