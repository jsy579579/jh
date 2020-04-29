package com.jh.paymentgateway.controller.tldh.Repository;

import com.jh.paymentgateway.controller.tldh.pojo.TLBindCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface TLBindCardRepostory extends JpaRepository<TLBindCard,Long>, JpaSpecificationExecutor<TLBindCard> {

    @Query("select tl from TLBindCard tl where tl.bankCard=:bankCard")
    TLBindCard getTLBindCardByBankCard(@Param("bankCard") String bankCard);
}
