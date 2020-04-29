package com.jh.paymentgateway.controller.hc.dao;


import com.jh.paymentgateway.controller.hc.pojo.HCDEBindCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface HCDEBindcardRepository extends JpaRepository<HCDEBindCard, Long>, JpaSpecificationExecutor<HCDEBindCard> {
    @Query("SELECT qy from HCDEBindCard qy where qy.bankCard =:bankcard")
    HCDEBindCard getHCDEBindCardByBankCard(@Param("bankcard") String bankcard);
}
