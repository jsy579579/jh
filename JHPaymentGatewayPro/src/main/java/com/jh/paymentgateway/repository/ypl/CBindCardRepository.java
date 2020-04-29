package com.jh.paymentgateway.repository.ypl;

import com.jh.paymentgateway.pojo.ypl.CBindCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CBindCardRepository extends JpaRepository<CBindCard, String>, JpaSpecificationExecutor<CBindCard> {

     @Query("select ypl from CBindCard ypl where ypl.bankCard=?1")
    CBindCard getCBindCardByBankCard(@Param("bankCard") String bankCard);
}
