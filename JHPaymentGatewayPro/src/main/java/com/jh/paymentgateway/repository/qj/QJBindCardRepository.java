package com.jh.paymentgateway.repository.qj;

import com.jh.paymentgateway.pojo.qj.QJBindCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface  QJBindCardRepository
        extends JpaRepository<QJBindCard, String>, JpaSpecificationExecutor<QJBindCard> {

    @Query("select qj from QJBindCard qj  where qj.bankCard=:bankCard")
    public QJBindCard getQJBindCardByBankCard(@Param("bankCard") String bankCard);



}
