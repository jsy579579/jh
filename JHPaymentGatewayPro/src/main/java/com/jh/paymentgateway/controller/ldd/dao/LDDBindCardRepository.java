package com.jh.paymentgateway.controller.ldd.dao;

import com.jh.paymentgateway.controller.ldd.pojo.LDDBindCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LDDBindCardRepository extends JpaRepository<LDDBindCard, Long>, JpaSpecificationExecutor<LDDBindCard> {

    @Query("select l from LDDBindCard l where l.bankCard=:bankCard")
    LDDBindCard queryByBankCard(@Param("bankCard") String bankCard);
}
