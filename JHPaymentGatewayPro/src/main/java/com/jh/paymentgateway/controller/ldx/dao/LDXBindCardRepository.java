package com.jh.paymentgateway.controller.ldx.dao;

import com.jh.paymentgateway.controller.ldx.pojo.LDXBindCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LDXBindCardRepository extends JpaRepository<LDXBindCard, Long>, JpaSpecificationExecutor<LDXBindCard> {

    @Query("select l from LDXBindCard l where l.bankCard=:bankCard")
    LDXBindCard queryByBankCard(@Param("bankCard") String bankCard);
}
