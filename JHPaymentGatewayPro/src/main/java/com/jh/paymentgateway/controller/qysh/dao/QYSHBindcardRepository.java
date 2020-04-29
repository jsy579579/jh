package com.jh.paymentgateway.controller.qysh.dao;

import com.jh.paymentgateway.controller.qysh.pojo.QYSHBindCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface QYSHBindcardRepository extends JpaRepository<QYSHBindCard, Long>, JpaSpecificationExecutor<QYSHBindCard> {
    @Query("SELECT qy from QYSHBindCard qy where qy.bankCard =:bankcard")
    QYSHBindCard getQYSHBindCardByBankCard(@Param("bankcard") String bankcard);
}
