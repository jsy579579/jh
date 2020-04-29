package com.jh.paymentgateway.repository.ypl;

import com.jh.paymentgateway.pojo.ypl.CBindCard;
import com.jh.paymentgateway.pojo.ypl.DBindCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DBindCardRepository  extends JpaRepository<DBindCard, String>, JpaSpecificationExecutor<DBindCard> {
    @Query("select ypl from DBindCard  ypl where ypl.debitCardNo=?1")
    DBindCard getDBindCardByDebitCardNo(@Param("debitCardNo") String debitCardNo);
}
