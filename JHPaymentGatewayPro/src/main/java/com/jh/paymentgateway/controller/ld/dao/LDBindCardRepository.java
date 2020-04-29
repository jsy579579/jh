package com.jh.paymentgateway.controller.ld.dao;

import com.jh.paymentgateway.controller.ld.pojo.LDBindCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LDBindCardRepository extends JpaRepository<LDBindCard, Long>, JpaSpecificationExecutor<LDBindCard> {

    @Query("SELECT l from LDBindCard l where l.bankCard =:bankcard")
    LDBindCard queryByBankCard(@Param("bankcard") String bankcard);
}
